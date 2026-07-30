package bflow.budget.services;

import bflow.budget.DTO.BudgetResponse;
import bflow.budget.entity.Budget;
import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.BudgetStatus;
import bflow.expenses.RepositoryExpense;
import bflow.expenses.BudgetExpenseData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Service for budget calculations.
 */
@Service
@RequiredArgsConstructor
public final class BudgetCalculationService {

    /**
     * Percentage multiplier for decimal conversion.
     */
    private static final int PERCENTAGE_MULTIPLIER = 100;

    /**
     * The expense repository.
     */
    private final RepositoryExpense repositoryExpense;

    /**
     * Calculate budget response from a budget entity.
     *
     * @param budget the budget entity
     * @return the budget response
     */
    public BudgetResponse calculate(final Budget budget) {
        LocalDate start = budget.getStartDate();
        LocalDate end = periodEnd(budget);

        BigDecimal spent;

        if (budget.getScope() == BudgetScope.WALLET) {
            spent = repositoryExpense.sumExpensesByWalletAndDateRange(
                    budget.getWallet().getId(),
                    start,
                    end
            );
        } else {
            spent = repositoryExpense.sumByCategoryAndDateRange(
                    budget.getCategoryId(),
                    start,
                    end
            );
        }

        return toResponse(budget, spent == null ? BigDecimal.ZERO : spent);
    }

    /**
     * Calculates a page's budgets from a single lightweight expense query.
     * The loaded budget wallets and projection query prevent N+1 queries while
     * keeping the established calculation semantics intact.
     *
     * @param budgets budgets in the requested page
     * @return calculated responses in the input order
     */
    public List<BudgetResponse> calculateAll(final List<Budget> budgets) {
        if (budgets.isEmpty()) {
            return List.of();
        }

        Collection<UUID> walletIds = budgets.stream()
                .map(budget -> budget.getWallet().getId())
                .collect(java.util.stream.Collectors.toSet());
        Collection<UUID> categoryIds = budgets.stream()
                .filter(budget -> budget.getScope() == BudgetScope.CATEGORY)
                .map(Budget::getCategoryId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        if (categoryIds.isEmpty()) {
            categoryIds = List.of(new UUID(0L, 0L));
        }
        LocalDate earliestStart = budgets.stream()
                .map(Budget::getStartDate)
                .min(LocalDate::compareTo)
                .orElseThrow();
        LocalDate latestEnd = budgets.stream()
                .map(this::periodEnd)
                .max(LocalDate::compareTo)
                .orElseThrow();
        List<BudgetExpenseData> expenses = repositoryExpense
                .findBudgetExpenseData(walletIds, categoryIds,
                        earliestStart, latestEnd);

        return budgets.stream()
                .map(budget -> toResponse(budget,
                        spentFor(budget, expenses)))
                .toList();
    }

    private BigDecimal spentFor(
            final Budget budget,
            final List<BudgetExpenseData> expenses
    ) {
        LocalDate start = budget.getStartDate();
        LocalDate end = periodEnd(budget);

        return expenses.stream()
                .filter(expense -> expense.getDate() != null
                        && !expense.getDate().isBefore(start)
                        && !expense.getDate().isAfter(end))
                .filter(expense -> budget.getScope() == BudgetScope.WALLET
                        ? budget.getWallet().getId()
                                .equals(expense.getWalletId())
                        : budget.getCategoryId()
                                .equals(expense.getCategoryId()))
                .map(BudgetExpenseData::getAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate periodEnd(final Budget budget) {
        return switch (budget.getPeriod()) {
            case WEEKLY -> budget.getStartDate().plusWeeks(1);
            case MONTHLY -> budget.getStartDate().plusMonths(1);
            case DAILY -> budget.getStartDate().plusDays(1);
        };
    }

    private BudgetResponse toResponse(
            final Budget budget,
            final BigDecimal spent
    ) {

        BigDecimal percentageDecimal = spent
                .multiply(BigDecimal.valueOf(PERCENTAGE_MULTIPLIER))
                .divide(budget.getAmount(), 2, RoundingMode.HALF_UP);

        int percentage = percentageDecimal.intValue();

        BudgetStatus status;

        final int percentageThreshold = 100;
        if (percentage >= percentageThreshold) {
            status = BudgetStatus.EXCEEDED;
        } else if (percentage >= budget.getThresholdCritical()) {
            status = BudgetStatus.CRITICAL;
        } else if (percentage >= budget.getThresholdWarning()) {
            status = BudgetStatus.WARNING;
        } else {
            status = BudgetStatus.OK;
        }

        BudgetResponse response = new BudgetResponse();
        response.setId(budget.getId());
        response.setName(budget.getName());
        response.setWalletId(budget.getWallet().getId());
        response.setPeriod(budget.getPeriod());
        response.setStartDate(budget.getStartDate());

        response.setBudgetLimit(budget.getAmount());
        response.setSpent(spent);

        BigDecimal remaining = budget.getAmount().subtract(spent);

        if (remaining.compareTo(BigDecimal.ZERO) < 0) {
            remaining = BigDecimal.ZERO;
        }

        response.setRemaining(remaining);

        response.setPercentage(percentage);
        response.setStatus(status);

        response.setThresholdWarning(budget.getThresholdWarning());
        response.setThresholdCritical(budget.getThresholdCritical());

        response.setCreatedAt(budget.getCreatedAt());

        return response;
    }
}
