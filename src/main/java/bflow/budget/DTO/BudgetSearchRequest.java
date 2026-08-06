package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.BudgetStatus;
import bflow.budget.enums.PeriodType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * DTO carrying optional search criteria for dynamic budget filtering.
 * Every field is optional; only non-null/non-blank values are applied
 * as predicates, which are combined using AND semantics.
 */
@Getter
@Setter
public class BudgetSearchRequest {

    /**
     * Case-insensitive partial match applied against the associated
     * wallet's name and the associated category's name.
     */
    private String name;

    /**
     * Restricts results to a specific wallet.
     */
    private UUID walletId;

    /**
     * Restricts results to a specific category.
     */
    private UUID categoryId;

    /**
     * Restricts results to a specific budget scope (WALLET or CATEGORY).
     */
    private BudgetScope scope;

    /**
     * Restricts results to a specific period type.
     */
    private PeriodType period;

    /**
     * Restricts results to budgets with a specific last alert status.
     */
    private BudgetStatus status;
}