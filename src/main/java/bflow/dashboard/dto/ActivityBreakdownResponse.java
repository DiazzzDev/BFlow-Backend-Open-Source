package bflow.dashboard.dto;

/**
 * "Activity breakdown" widget: share of this month's transactions that
 * are incomes, expenses, and transfers, plus overall activity volume
 * change vs last month.
 *
 * @param totalTransactions total incomes + expenses + transfers this month.
 * @param incomePercentage share of total transactions that are incomes.
 * @param expensePercentage share of total transactions that are expenses.
 * @param transferPercentage share of total transactions that are transfers.
 * @param activityChangePercentage % change in total transaction count vs
 *        last month (positive = more active, negative = less active).
 */
public record ActivityBreakdownResponse(
        long totalTransactions,
        Double incomePercentage,
        Double expensePercentage,
        Double transferPercentage,
        Double activityChangePercentage
) { }
