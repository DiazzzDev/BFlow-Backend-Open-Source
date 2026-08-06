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

    private UUID id;
    private UUID walletId;
    private String walletName;
    private Currency currency;

    private UUID categoryId;
    private String categoryName;

    private BudgetScope scope;
    private PeriodType period;
    private BudgetStatus status;

    private LocalDate startDate;
    private LocalDate endDate;
    private int daysLeft;
    private int daysElapsed;

    private BigDecimal budgetLimit;
    private BigDecimal spent;
    private BigDecimal remaining;
    private double percentage;

    private Integer thresholdWarning;
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

    private List<SpendingTrendPoint> spendingTrend;
    private List<RecentActivityItem> recentActivity;
}
