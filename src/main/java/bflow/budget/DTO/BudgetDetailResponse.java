package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.BudgetStatus;
import bflow.budget.enums.PeriodType;
import bflow.wallet.enums.Currency;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Aggregated detail view for a single budget, used by the budget
 * dashboard UI (overview, spending trend and recent activity).
 */
@Getter
@Setter
public class BudgetDetailResponse {

    /** Budget identifier. */
    private UUID id;

    /** Identifier of the associated wallet. */
    private UUID walletId;

    /** Name of the associated wallet. */
    private String walletName;

    /** Currency used by the wallet and budget. */
    private Currency currency;

    /** Identifier of the associated category. */
    private UUID categoryId;

    /** Name of the associated category. */
    private String categoryName;

    /** Scope of the budget. */
    private BudgetScope scope;

    /** Budget period type. */
    private PeriodType period;

    /** Current budget status. */
    private BudgetStatus status;

    /** Budget start date. */
    private LocalDate startDate;

    /** Budget end date. */
    private LocalDate endDate;

    /** Remaining days until the budget period ends. */
    private int daysLeft;

    /** Number of elapsed days within the budget period. */
    private int daysElapsed;

    /** Maximum amount allocated to the budget. */
    private BigDecimal budgetLimit;

    /** Total amount spent so far. */
    private BigDecimal spent;

    /** Remaining amount available in the budget. */
    private BigDecimal remaining;

    /** Percentage of the budget that has been consumed. */
    private double percentage;

    /** Warning threshold percentage. */
    private Integer thresholdWarning;

    /** Critical threshold percentage. */
    private Integer thresholdCritical;

    /** Number of transactions counted within the current period. */
    private int transactionCount;

    /** Average amount spent per elapsed day in the current period. */
    private BigDecimal averageDailySpend;

    /**
     * Projected total spend at period end, based on the current
     * average daily spend. Null if no days have elapsed yet.
     */
    private BigDecimal projectedTotal;

    /** Spending trend points for chart visualization. */
    private List<SpendingTrendPoint> spendingTrend;

    /** Recent transactions associated with the budget. */
    private List<RecentActivityItem> recentActivity;
}
