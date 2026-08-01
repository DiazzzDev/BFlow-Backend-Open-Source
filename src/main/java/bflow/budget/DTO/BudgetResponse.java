package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
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
     * Wallet ID.
     */
    private UUID walletId;

    /**
     * Wallet name.
     */
    private String walletName;

    /**
     * Category ID (only when scope = CATEGORY).
     */
    private UUID categoryId;

    /**
     * Category name (only when scope = CATEGORY).
     */
    private String categoryName;

    /**
     * Budget scope.
     */
    private BudgetScope scope;

    /**
     * Budget period type.
     */
    private PeriodType period;

    /**
     * Budget start date.
     */
    private LocalDate startDate;

    /**
     * Budget limit amount.
     */
    private BigDecimal budgetLimit;

    /**
     * Amount spent.
     */
    private BigDecimal spent;

    /**
     * Remaining amount.
     */
    private BigDecimal remaining;

    /**
     * Percentage used.
     */
    private Integer percentage;

    /**
     * Budget status.
     */
    private BudgetStatus status;

    /**
     * Warning threshold.
     */
    private Integer thresholdWarning;

    /**
     * Critical threshold.
     */
    private Integer thresholdCritical;

    /**
     * Creation timestamp.
     */
    private Instant createdAt;

    /**
     * Last updated timestamp.
     */
    private Instant updatedAt;
}