package bflow.budget.enums;

/**
 * Budget scope enumeration.
 */
public enum BudgetScope {
    /** Budget scoped to an entire wallet, all categories. */
    WALLET,
    /** Budget scoped to a category within a specific wallet. */
    WALLET_CATEGORY,
    /** Budget scoped to a category across every wallet the user
     *  participates in, regardless of role. */
    CATEGORY_GLOBAL
}
