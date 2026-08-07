package bflow.budget.services;

import bflow.budget.DTO.BudgetResponse;
import bflow.budget.entity.Budget;
import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.BudgetStatus;
import bflow.expenses.RepositoryExpense;
import bflow.wallet.repository.RepositoryWalletUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
     * Service for managing budget lifecycle operations.
     */
    private final BudgetLifecycleService lifecycleService;

    private final RepositoryWalletUser repositoryWalletUser;

    /**
     * Calculate budget response from a budget entity.
     *
     * @param budget the budget entity
     * @return the budget response
     */
    public BudgetResponse calculate(final Budget budget) {

        LocalDate start = budget.getStartDate();
        LocalDate end = lifecycleService.calculateEndDate(budget);

        BigDecimal spent = calculateSpent(budget, start, end);

        BigDecimal amount = budget.getAmount();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Budget amount must be greater than 0"
            );
        }

        BigDecimal percentageDecimal = spent
                .multiply(BigDecimal.valueOf(PERCENTAGE_MULTIPLIER))
                .divide(amount, 2, RoundingMode.HALF_UP);

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

        if (budget.getWallet() != null) {
            response.setWalletId(budget.getWallet().getId());
        }

        response.setScope(budget.getScope());
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

    /**
     * Resolves the amount spent for a budget's current period, scoped
     * correctly to its wallet (and category, when applicable).
     *
     * @param budget the budget entity
     * @param start the period start date
     * @param end the period end date
     * @return the total spent amount, never {@code null}
     */
    private BigDecimal calculateSpent(
            final Budget budget,
            final LocalDate start,
            final LocalDate end
    ) {
        BudgetScope scope = budget.getScope();

        if (scope == null) {
            throw new IllegalStateException(
                    "Budget " + budget.getId() + " has no scope defined"
            );
        }

        BigDecimal spent = switch (scope) {
            case WALLET -> repositoryExpense.sumExpensesByWalletAndDateRange(
                    budget.getWallet().getId(), start, end
            );
            case WALLET_CATEGORY -> {
                requireCategory(budget);
                yield repositoryExpense.sumByWalletAndCategoryAndDateRange(
                        budget.getWallet().getId(),
                        budget.getCategory().getId(),
                        start,
                        end
                );
            }
            case CATEGORY_GLOBAL -> {
                requireCategory(budget);
                List<UUID> walletIds = repositoryWalletUser
                        .findWalletIdsByUserId(budget.getUser().getId());

                if (walletIds.isEmpty()) {
                    yield BigDecimal.ZERO;
                }

                yield repositoryExpense.sumByWalletsAndCategoryAndDateRange(
                        walletIds,
                        budget.getCategory().getId(),
                        start,
                        end
                );
            }
        };

        return spent != null ? spent : BigDecimal.ZERO;
    }

    private void requireCategory(final Budget budget) {
        if (budget.getCategory() == null) {
            throw new IllegalStateException(
                    "Budget " + budget.getId()
                            + " has scope " + budget.getScope()
                            + " but no category set"
            );
        }
    }
}