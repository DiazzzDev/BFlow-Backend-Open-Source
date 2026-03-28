package bflow.budget.enums;

/**
 * Budget status enumeration.
 */
public enum BudgetStatus {
    /**
     * Budget is below the warning threshold.
     */
    OK,
    /**
     * Budget has exceeded the warning threshold.
     */
    WARNING,
    /**
     * Budget has exceeded the critical threshold.
     */
    CRITICAL,
    /**
     * Budget has been exceeded (100%).
     */
    EXCEEDED
}
