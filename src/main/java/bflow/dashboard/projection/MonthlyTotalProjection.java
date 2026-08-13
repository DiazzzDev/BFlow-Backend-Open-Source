package bflow.dashboard.projection;

import java.math.BigDecimal;

/**
 * Projection for month-grouped sum queries (income/expense statistics).
 */
public interface MonthlyTotalProjection {

    /**
     * The calendar month number (1-12) for this aggregation row.
     *
     * @return the month number
     */
    Number getMonth();

    /**
     * The summed amount for this month.
     *
     * @return the total amount
     */
    BigDecimal getTotal();
}
