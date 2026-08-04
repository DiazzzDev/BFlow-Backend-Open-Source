package bflow.common.financial;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builds the unified transaction history (incomes + expenses + transfers)
 * via a native UNION ALL query, restricted to the wallets the
 * authenticated user belongs to.
 *
 * Specifications don't apply here because the result set spans three
 * independent entities with no common JPA root; filters are applied
 * as parameterized SQL predicates instead (never string-concatenated
 * user input) to preserve the same "dynamic filtering" intent.
 *
 * When {@link TransactionSearchCriteria#type()} is set, only the matching
 * branch is executed — no UNION ALL over the other two tables — to keep
 * single-type lookups (?type=INCOME) as cheap as querying that table alone.
 */
@Repository
public class RepositoryTransactionHistory {

    /** Columns the client is allowed to sort by (prevents SQL injection via ORDER BY). */
    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "date", "txn_date",
            "amount", "amount"
    );

    private static final String DEFAULT_SORT_COLUMN = "txn_date";
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    private final NamedParameterJdbcTemplate jdbc;

    public RepositoryTransactionHistory(final NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final String EXPENSE_BRANCH = """
        SELECT
            e.id::text AS id, 'EXPENSE' AS type, e.title AS title,
            e.description AS description, -e.amount AS amount,
            e.date::timestamp AS txn_date,
            e.wallet_id::text AS wallet_id, w1.name AS wallet_name,
            NULL::text AS counterpart_wallet_id,
            NULL::text AS counterpart_wallet_name,
            e.category_id::text AS category_id, c1.name AS category_name,
            c1.icon AS category_icon, c1.color AS category_color,
            e.user_id::text AS contributor_id, u1.email AS contributor_name,
            NULL::text AS status, e.source AS source
        FROM expenses e
        JOIN wallets w1 ON w1.id = e.wallet_id
        LEFT JOIN categories c1 ON c1.id = e.category_id
        JOIN users u1 ON u1.id = e.user_id
        WHERE e.wallet_id IN (:walletIds)
          AND (:hasQuery = false OR UPPER(e.title) LIKE :queryPattern
               OR UPPER(e.description) LIKE :queryPattern)
        """;

    private static final String INCOME_BRANCH = """
    SELECT
        i.id::text AS id,
        'INCOME' AS type,
        i.title AS title,
        i.description AS description,
        i.amount AS amount,
        i.date::timestamp AS txn_date,
        i.wallet_id::text AS wallet_id,
        w2.name AS wallet_name,
        NULL::text AS counterpart_wallet_id,
        NULL::text AS counterpart_wallet_name,
        i.category_id::text AS category_id,
        c2.name AS category_name,
        c2.icon AS category_icon,
        c2.color AS category_color,
        i.user_id::text AS contributor_id,
        u2.email AS contributor_name,
        NULL::text AS status,
        i.source AS source
    FROM incomes i
    JOIN wallets w2 ON w2.id = i.wallet_id
    LEFT JOIN categories c2 ON c2.id = i.category_id
    JOIN users u2 ON u2.id = i.user_id
    WHERE i.wallet_id IN (:walletIds)
      AND (:hasQuery = false OR UPPER(i.title) LIKE :queryPattern
           OR UPPER(i.description) LIKE :queryPattern)
    """;

    private static final String TRANSFER_BRANCH = """
    SELECT
        t.id::text AS id,
        'TRANSFER' AS type,
        'Transfer' AS title,
        t.description AS description,
        CASE
            WHEN CAST(:primaryWalletId AS uuid) IS NULL THEN t.amount
            WHEN t.to_wallet_id = CAST(:primaryWalletId AS uuid) THEN t.amount
            ELSE -t.amount
        END AS amount,
        t.created_at::timestamp AS txn_date,
        t.from_wallet_id::text AS wallet_id,
        w3f.name AS wallet_name,
        t.to_wallet_id::text AS counterpart_wallet_id,
        w3t.name AS counterpart_wallet_name,
        NULL::text AS category_id,
        NULL::text AS category_name,
        NULL::text AS category_icon,
        NULL::text AS category_color,
        t.user_id::text AS contributor_id,
        u3.email AS contributor_name,
        t.status::text AS status,
        NULL::text AS source
    FROM transfer t
    JOIN wallets w3f ON w3f.id = t.from_wallet_id
    JOIN wallets w3t ON w3t.id = t.to_wallet_id
    JOIN users u3 ON u3.id = t.user_id
    WHERE (t.from_wallet_id IN (:walletIds) OR t.to_wallet_id IN (:walletIds))
      AND (:hasQuery = false OR UPPER(t.description) LIKE :queryPattern)
    """;

    private static final Map<TransactionType, String> BRANCHES_BY_TYPE =
            new EnumMap<>(TransactionType.class);

    static {
        BRANCHES_BY_TYPE.put(TransactionType.EXPENSE, EXPENSE_BRANCH);
        BRANCHES_BY_TYPE.put(TransactionType.INCOME, INCOME_BRANCH);
        BRANCHES_BY_TYPE.put(TransactionType.TRANSFER, TRANSFER_BRANCH);
    }

    /**
     * Executes the unified, paginated transaction history.
     *
     * @param walletIds the set of wallet IDs the user is authorized to see.
     * @param criteria dynamic filters (search text, type, primary wallet for sign).
     * @param pageable pagination + sort (only "date"/"amount" honored; defaults to date desc).
     * @return a page of unified transaction entries.
     */
    public Page<TransactionResponse> search(
            final List<UUID> walletIds,
            final TransactionSearchCriteria criteria,
            final Pageable pageable
    ) {
        if (walletIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // Only build the branches relevant to the requested type — if
        // ?type=INCOME, EXPENSE and TRANSFER never touch the DB at all.
        String unifiedSelect = criteria.type() != null
                ? BRANCHES_BY_TYPE.get(criteria.type())
                : String.join(
                "\nUNION ALL\n",
                EXPENSE_BRANCH, INCOME_BRANCH, TRANSFER_BRANCH
        );

        boolean hasQuery = criteria.query() != null && !criteria.query().isBlank();
        String queryPattern = hasQuery
                ? "%" + criteria.query().trim().toUpperCase() + "%"
                : null;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("walletIds", walletIds)
                .addValue("hasQuery", hasQuery)
                .addValue("queryPattern", queryPattern)
                .addValue("primaryWalletId", criteria.walletId());

        String orderByClause = resolveOrderBy(pageable.getSort());

        String dataSql = "SELECT * FROM (" + unifiedSelect + ") unified "
                + "ORDER BY " + orderByClause + " LIMIT :limit OFFSET :offset";
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        List<TransactionResponse> content = jdbc.query(dataSql, params, (rs, rowNum) -> {
            TransactionResponse dto = new TransactionResponse();
            dto.setId(rs.getString("id"));
            dto.setType(TransactionType.valueOf(rs.getString("type")));
            dto.setTitle(rs.getString("title"));
            dto.setDescription(rs.getString("description"));
            dto.setAmount(rs.getBigDecimal("amount"));
            Timestamp ts = rs.getTimestamp("txn_date");
            dto.setDate(ts != null ? ts.toInstant() : null);
            dto.setWalletId(rs.getString("wallet_id"));
            dto.setWalletName(rs.getString("wallet_name"));
            dto.setCounterpartWalletId(rs.getString("counterpart_wallet_id"));
            dto.setCounterpartWalletName(rs.getString("counterpart_wallet_name"));
            dto.setCategoryId(rs.getString("category_id"));
            dto.setCategoryName(rs.getString("category_name"));
            dto.setCategoryIcon(rs.getString("category_icon"));
            dto.setCategoryColor(rs.getString("category_color"));
            dto.setContributorId(rs.getString("contributor_id"));
            dto.setContributorName(rs.getString("contributor_name"));
            dto.setStatus(rs.getString("status"));
            dto.setSource(rs.getString("source"));
            return dto;
        });

        String countSql = "SELECT COUNT(*) FROM (" + unifiedSelect + ") unified";
        Long total = jdbc.queryForObject(countSql, params, Long.class);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * Resolves a safe ORDER BY clause from the request's Sort, restricted
     * to a whitelist of sortable columns. Falls back to txn_date DESC.
     * Only the first Sort.Order is honored (single-column sort is enough
     * for this use case and keeps the LIMIT/OFFSET pagination stable).
     */
    private String resolveOrderBy(final Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return DEFAULT_SORT_COLUMN + " " + DEFAULT_SORT_DIRECTION;
        }

        Sort.Order order = sort.iterator().next();
        String column = SORTABLE_COLUMNS.get(order.getProperty().toLowerCase());

        if (column == null) {
            return DEFAULT_SORT_COLUMN + " " + DEFAULT_SORT_DIRECTION;
        }

        String direction = order.isAscending() ? "ASC" : "DESC";
        return column + " " + direction;
    }
}