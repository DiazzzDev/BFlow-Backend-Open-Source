package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.PeriodType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Optional filters accepted by the budget search endpoint.
 * Add future database-backed filters here and compose them in
 * {@code BudgetSpecifications}; no repository method proliferation is needed.
 */
@Getter
@Setter
public class BudgetSearchCriteria {

    /** Case-insensitive text matched against the budget name. */
    private String name;

    /** Restricts results to a wallet. */
    private UUID walletId;

    /** Restricts results to a budget period. */
    private PeriodType period;

    /** Restricts results to a budget scope. */
    private BudgetScope scope;

    /** Includes budgets starting on or after this date. */
    private LocalDate startDateFrom;

    /** Includes budgets starting on or before this date. */
    private LocalDate startDateTo;
}
