package bflow.common.financial;

/**
 * Discriminates the origin of a unified transaction history entry.
 */
public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}