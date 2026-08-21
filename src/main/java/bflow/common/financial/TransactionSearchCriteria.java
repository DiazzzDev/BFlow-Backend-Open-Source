package bflow.common.financial;

import java.util.UUID;

/**
 * Dynamic filter criteria for the unified transaction history query.
 *
 * Extensible: add fields here (date range, category, etc.)
 * without changing the repository's public API.
 *
 * @param query optional search text.
 * @param walletId wallet identifier when filtering a single wallet;
 *                 {@code null} for global history.
 * @param type optional transaction type filter;
 *             {@code null} includes all transaction types.
 */
public record TransactionSearchCriteria(
        String query,
        UUID walletId,
        TransactionType type
) {

    /**
     * Creates criteria for the global transaction history.
     *
     * @param query optional search text.
     * @param type optional transaction type filter.
     * @return transaction search criteria.
     */
    public static TransactionSearchCriteria global(
            final String query,
            final TransactionType type
    ) {
        return new TransactionSearchCriteria(query, null, type);
    }

    /**
     * Creates criteria for a wallet-specific transaction history.
     *
     * @param query optional search text.
     * @param walletId wallet identifier.
     * @param type optional transaction type filter.
     * @return transaction search criteria.
     */
    public static TransactionSearchCriteria forWallet(
            final String query,
            final UUID walletId,
            final TransactionType type
    ) {
        return new TransactionSearchCriteria(query, walletId, type);
    }
}
