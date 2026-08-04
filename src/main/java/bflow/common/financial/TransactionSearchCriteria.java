package bflow.common.financial;

import java.util.UUID;

/**
 * Dynamic filter criteria for the unified transaction history query.
 * Extensible: add fields here (date range, category, etc.)
 * without touching the repository's public API.
 */
public record TransactionSearchCriteria(
        String query,
        UUID walletId,       // present only for the wallet-scoped endpoint
        TransactionType type // null = all types (INCOME, EXPENSE, TRANSFER)
) {
    public static TransactionSearchCriteria global(
            final String query, final TransactionType type
    ) {
        return new TransactionSearchCriteria(query, null, type);
    }

    public static TransactionSearchCriteria forWallet(
            final String query, final UUID walletId, final TransactionType type
    ) {
        return new TransactionSearchCriteria(query, walletId, type);
    }
}
