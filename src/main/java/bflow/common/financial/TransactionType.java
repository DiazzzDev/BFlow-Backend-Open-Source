package bflow.common.financial;

/**
 * Discriminates the origin of a unified transaction history entry.
 */
public enum TransactionType {

    /**
     * Money received by a wallet.
     */
    INCOME,

    /**
     * Money spent from a wallet.
     */
    EXPENSE,

    /**
     * Transfer of funds between wallets.
     */
    TRANSFER
}
