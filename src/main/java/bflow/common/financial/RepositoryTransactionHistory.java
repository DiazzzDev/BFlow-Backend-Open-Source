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

    /**
     * Column name for the transaction amount — shared between the
     * sortable-columns whitelist and the result-set mapping below.
     */
    private static final String AMOUNT_COLUMN = "amount";

    /**
     * Default column used for sorting.
     */
    private static final String DEFAULT_SORT_COLUMN = "txn_date";

    /**
     * Default sort direction.
     */
    private static final String DEFAULT_SORT_DIRECTION = "DESC";

    /**
     * Columns the client is allowed to sort by
     * (prevents SQL injection via ORDER BY).
     */
    private static final Map<String, String> SORTABLE_COLUMNS = Map.of(
            "date", DEFAULT_SORT_COLUMN,
            AMOUNT_COLUMN, AMOUNT_COLUMN
    );

    /**
     * JDBC template used to execute native SQL queries.
     */
    private final NamedParameterJdbcTemplate jdbc;

    /**
     * Creates a new transaction history repository.
     *
     * @param jdbcTemplate JDBC template used to execute native queries.
     */
    public RepositoryTransactionHistory(
            final NamedParameterJdbcTemplate jdbcTemplate
    ) {
        this.jdbc = jdbcTemplate;
    }

    /**
     * SQL branch for expense transactions.
     */
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
            e.user_id::text AS contributor_id, u1.name AS contributor_name,
            u1.email AS contributor_email,
            u1.picture_url AS contributor_picture_url,
            wm1.member_count AS member_count,
            NULL::text AS status, e.source AS source
        FROM expenses e
        JOIN wallets w1 ON w1.id = e.wallet_id
        LEFT JOIN categories c1 ON c1.id = e.category_id
        JOIN users u1 ON u1.id = e.user_id
        JOIN (
            SELECT wallet_id, COUNT(*) AS member_count
            FROM wallet_users
            GROUP BY wallet_id
        ) wm1 ON wm1.wallet_id = e.wallet_id
        WHERE e.wallet_id IN (:walletIds)
          AND (:hasQuery = false OR UPPER(e.title) LIKE :queryPattern
               OR UPPER(e.description) LIKE :queryPattern)
          AND (:hasContributorFilter = false
               OR e.user_id IN (:contributorIds))
        """;

    /**
     * SQL branch for income transactions.
     */
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
        u2.name AS contributor_name,
        u2.email AS contributor_email,
        u2.picture_url AS contributor_picture_url,
        wm2.member_count AS member_count,
        NULL::text AS status,
        i.source AS source
    FROM incomes i
    JOIN wallets w2 ON w2.id = i.wallet_id
    LEFT JOIN categories c2 ON c2.id = i.category_id
    JOIN users u2 ON u2.id = i.user_id
    JOIN (
        SELECT wallet_id, COUNT(*) AS member_count
        FROM wallet_users
        GROUP BY wallet_id
    ) wm2 ON wm2.wallet_id = i.wallet_id
    WHERE i.wallet_id IN (:walletIds)
      AND (:hasQuery = false OR UPPER(i.title) LIKE :queryPattern
           OR UPPER(i.description) LIKE :queryPattern)
      AND (:hasContributorFilter = false
           OR i.user_id IN (:contributorIds))
    """;

    /**
     * SQL branch for transfer transactions.
     */
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
        u3.name AS contributor_name,
        u3.email AS contributor_email,
        u3.picture_url AS contributor_picture_url,
        wm3.member_count AS member_count,
        t.status::text AS status,
        NULL::text AS source
    FROM transfer t
    JOIN wallets w3f ON w3f.id = t.from_wallet_id
    JOIN wallets w3t ON w3t.id = t.to_wallet_id
    JOIN users u3 ON u3.id = t.user_id
    JOIN (
        SELECT wallet_id, COUNT(*) AS member_count
        FROM wallet_users
        GROUP BY wallet_id
    ) wm3 ON wm3.wallet_id = t.from_wallet_id
    WHERE (t.from_wallet_id IN (:walletIds) OR t.to_wallet_id IN (:walletIds))
      AND (:hasQuery = false OR UPPER(t.description) LIKE :queryPattern)
      AND (:hasContributorFilter = false
           OR t.user_id IN (:contributorIds))
    """;

    /**
     * Maps each transaction type to its SQL branch.
     */
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
     * @param criteria dynamic filters
     * (search text, type, contributor ids, primary wallet for sign).
     * @param pageable pagination + sort
     * (only "date"/"amount" honored; defaults to date desc).
     * @return a page of unified transaction entries.
     */
    // SonarQube (java:S2077) flags dataSql/countSql as dynamically
    // formatted SQL because they're built with string concatenation.
    // This is a false positive: every concatenated piece is either a
    // static final SQL constant (EXPENSE_BRANCH/INCOME_BRANCH/
    // TRANSFER_BRANCH) or the output of resolveOrderBy(), which only
    // ever returns a column name from the SORTABLE_COLUMNS whitelist
    // plus a hardcoded "ASC"/"DESC". No request-supplied value is ever
    // concatenated into the SQL text — all actual filter values are
    // bound as named parameters (:walletIds, :queryPattern, etc.).
    @SuppressWarnings("java:S2077")
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

        boolean hasQuery = criteria.query() != null
                && !criteria.query().isBlank();

        String queryPattern = hasQuery
                ? "%" + criteria.query().trim().toUpperCase() + "%"
                : null;

        boolean hasContributorFilter = criteria.contributorIds() != null
                && !criteria.contributorIds().isEmpty();

        // NamedParameterJdbcTemplate expands "IN (:x)" before the SQL
        // engine ever sees the short-circuiting AND/OR, so :x must
        // always be non-empty — a sentinel UUID that can never match
        // a real row stands in when the filter isn't active.
        List<UUID> contributorIds = hasContributorFilter
                ? criteria.contributorIds()
                : List.of(new UUID(0L, 0L));

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("walletIds", walletIds)
                .addValue("hasQuery", hasQuery)
                .addValue("queryPattern", queryPattern)
                .addValue("primaryWalletId", criteria.walletId())
                .addValue("hasContributorFilter", hasContributorFilter)
                .addValue("contributorIds", contributorIds);

        String orderByClause = resolveOrderBy(pageable.getSort());

        String dataSql = "SELECT * FROM (" + unifiedSelect + ") unified "
                + "ORDER BY " + orderByClause + " LIMIT :limit OFFSET :offset";
        params.addValue("limit", pageable.getPageSize());
        params.addValue("offset", pageable.getOffset());

        List<TransactionResponse> content =
                jdbc.query(dataSql, params, (rs, rowNum) -> {
                    TransactionResponse dto = new TransactionResponse();
                    dto.setId(rs.getString("id"));
                    dto.setType(TransactionType.valueOf(rs.getString("type")));
                    dto.setTitle(rs.getString("title"));
                    dto.setDescription(rs.getString("description"));
                    dto.setAmount(rs.getBigDecimal(AMOUNT_COLUMN));
                    Timestamp ts = rs.getTimestamp("txn_date");
                    dto.setDate(ts != null ? ts.toInstant() : null);
                    dto.setWalletId(rs.getString("wallet_id"));
                    dto.setWalletName(rs.getString("wallet_name"));
                    dto.setCounterpartWalletId(
                            rs.getString("counterpart_wallet_id")
                    );

                    dto.setCounterpartWalletName(
                            rs.getString("counterpart_wallet_name")
                    );

                    dto.setCategoryId(rs.getString("category_id"));
                    dto.setCategoryName(rs.getString("category_name"));
                    dto.setCategoryIcon(rs.getString("category_icon"));
                    dto.setCategoryColor(rs.getString("category_color"));
                    dto.setContributorId(rs.getString("contributor_id"));

                    // Only worth showing who made it when someone else could
                    // have — on a solo wallet it's always you.
                    if (rs.getInt("member_count") > 1) {
                        dto.setContributorName(
                            rs.getString("contributor_name")
                        );
                        dto.setContributorEmail(
                            rs.getString("contributor_email")
                        );
                        dto.setContributorPictureUrl(
                                rs.getString("contributor_picture_url")
                        );
                    }

                    dto.setStatus(rs.getString("status"));
                    dto.setSource(rs.getString("source"));
                    return dto;
                });

        String countSql =
                "SELECT COUNT(*) FROM (" + unifiedSelect + ") unified";
        Long total = jdbc.queryForObject(countSql, params, Long.class);

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    /**
     * Resolves a safe ORDER BY clause from the requested sort.
     *
     * Only whitelisted columns are allowed to prevent SQL injection.
     * If no valid sort is provided, the default ordering is
     * {@code txn_date DESC}.
     *
     * @param sort requested sort definition.
     * @return SQL ORDER BY clause.
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
