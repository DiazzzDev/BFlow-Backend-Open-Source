package bflow.budget.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single point on the cumulative spending trend chart.
 */
@Getter
@AllArgsConstructor
public class SpendingTrendPoint {
    /** Day of the budget period (1-indexed, matches the X axis). */
    private int dayIndex;
    /** Calendar date this point corresponds to. */
    private LocalDate date;
    /** Cumulative amount spent up to and including this day. */
    private BigDecimal cumulativeAmount;
}
