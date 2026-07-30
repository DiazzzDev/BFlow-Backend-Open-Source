package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.PeriodType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for budget creation request.
 */
@Getter
@Setter
@NoArgsConstructor
public class BudgetRequest {

    /**
     * Threshold validation maximum.
     */
    private static final int THRESHOLD_MAX = 99;

    /**
     * Warning threshold default value.
     */
    private static final int WARNING_THRESHOLD_DEFAULT = 70;

    /**
     * Critical threshold default value.
     */
    private static final int CRITICAL_THRESHOLD_DEFAULT = 90;

    /** The maximum permitted budget-name length. */
    private static final int NAME_MAX_LENGTH = 100;

    /** The name shown to users and used for searching budgets. */
    @NotBlank
    @Size(max = NAME_MAX_LENGTH)
    private String name;

    /**
     * The wallet ID for the budget.
     */
    @NotNull
    private UUID walletId;

    /**
     * The budget amount.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

    /**
     * The budget period type.
     */
    @NotNull
    private PeriodType period;

    /**
     * The budget start date.
     */
    @NotNull
    private LocalDate startDate;

    /**
     * The category ID (required if scope is CATEGORY).
     */
    private UUID categoryId;

    /**
     * The warning threshold percentage.
     */
    @Min(1)
    @Max(THRESHOLD_MAX)
    private Integer thresholdWarning = WARNING_THRESHOLD_DEFAULT;

    /**
     * The critical threshold percentage.
     */
    @Min(1)
    @Max(THRESHOLD_MAX)
    private Integer thresholdCritical = CRITICAL_THRESHOLD_DEFAULT;

    /**
     * The budget scope (WALLET or CATEGORY).
     */
    private BudgetScope scope;
}
