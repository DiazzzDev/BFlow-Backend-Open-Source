package bflow.budget.DTO;

import bflow.budget.enums.BudgetStatus;
import bflow.budget.enums.PeriodType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for budget response.
 */
@Getter
@Setter
public class BudgetResponse {
    /**
     * The budget ID.
     */
    private UUID id;
    /**
     * The wallet ID.
     */
    private UUID walletId;
    /**
     * The budget period type.
     */
    private PeriodType period;
    /**
     * The budget start date.
     */
    private LocalDate startDate;

    /**
     * The budget limit amount.
     */
    private BigDecimal budgetLimit;
    /**
     * The amount spent.
     */
    private BigDecimal spent;
    /**
     * The remaining budget amount.
     */
    private BigDecimal remaining;
    /**
     * The percentage used.
     */
    private Integer percentage;

    /**
     * The budget status.
     */
    private BudgetStatus status;
    /**
     * The warning threshold.
     */
    private Integer thresholdWarning;
    /**
     * The critical threshold.
     */
    private Integer thresholdCritical;

    /**
     * The creation timestamp.
     */
    private Instant createdAt;
}
