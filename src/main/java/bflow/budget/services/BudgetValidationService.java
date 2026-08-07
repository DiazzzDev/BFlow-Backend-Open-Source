package bflow.budget.services;

import bflow.budget.enums.BudgetScope;
import bflow.common.exception.InvalidBudgetDateException;
import bflow.common.exception.InvalidBudgetScopeException;
import bflow.common.exception.InvalidBudgetThresholdException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public final class BudgetValidationService {

    /**
     * Validate that the budget start date is valid and not in the future.
     *
     * @param startDate the start date to validate
     * @throws InvalidBudgetDateException if the date is invalid
     */
    public void validateStartDate(final LocalDate startDate) {

        if (startDate == null) {
            throw new InvalidBudgetDateException(
                    "Start date is required"
            );
        }

        if (startDate.isAfter(LocalDate.now())) {
            throw new InvalidBudgetDateException(
                    "Start date cannot be in the future"
            );
        }
    }

    /**
     * Validate budget constraints including thresholds and scope.
     *
     * @param scope the budget scope
     * @param categoryId the category ID (required for CATEGORY scope)
     * @param warning the warning threshold percentage
     * @param critical the critical threshold percentage
     * @throws InvalidBudgetThresholdException if thresholds are invalid
     * @throws InvalidBudgetScopeException if scope configuration is invalid
     */
    public void validateBudgetConstraints(
            final BudgetScope scope,
            final UUID walletId,
            final UUID categoryId,
            final Integer warning,
            final Integer critical
    ) {
        validateThresholds(warning, critical);
        validateBudgetScope(scope, walletId, categoryId);
    }

    /**
     * Validate that warning threshold is lower than critical threshold.
     *
     * @param warning the warning threshold percentage
     * @param critical the critical threshold percentage
     * @throws InvalidBudgetThresholdException if warning >= critical
     */
    public void validateThresholds(
            final Integer warning,
            final Integer critical
    ) {

        if (warning != null
                && critical != null
                && warning >= critical) {

            throw new InvalidBudgetThresholdException(
                    "Warning threshold must be less than critical"
            );
        }
    }

    /**
     * Validate that the budget amount is positive and not null.
     *
     * @param amount the amount to validate
     * @throws IllegalArgumentException if amount is null or less than 1
     */
    public void validateAmount(final BigDecimal amount) {

        if (amount == null) {
            throw new IllegalArgumentException(
                    "Budget amount is required"
            );
        }

        if (amount.compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException(
                    "Budget amount must be greater than or equal to 1"
            );
        }
    }

    /**
     * Validate that wallet/category presence matches the budget's scope.
     *
     * @param scope the budget scope
     * @param walletId the wallet ID (nullable depending on scope)
     * @param categoryId the category ID (nullable depending on scope)
     * @throws InvalidBudgetScopeException if the combination is invalid
     */
    public void validateBudgetScope(
            final BudgetScope scope,
            final UUID walletId,
            final UUID categoryId
    ) {
        switch (scope) {
            case WALLET -> {
                if (walletId == null) {
                    throw new InvalidBudgetScopeException(
                            "WALLET scope requires walletId"
                    );
                }
                if (categoryId != null) {
                    throw new InvalidBudgetScopeException(
                            "WALLET scope must not have a categoryId"
                    );
                }
            }
            case WALLET_CATEGORY -> {
                if (walletId == null || categoryId == null) {
                    throw new InvalidBudgetScopeException(
                            "WALLET_CATEGORY scope requires both "
                                    + "walletId and categoryId"
                    );
                }
            }
            case CATEGORY_GLOBAL -> {
                if (categoryId == null) {
                    throw new InvalidBudgetScopeException(
                            "CATEGORY_GLOBAL scope requires categoryId"
                    );
                }
                if (walletId != null) {
                    throw new InvalidBudgetScopeException(
                            "CATEGORY_GLOBAL scope must not have a walletId"
                    );
                }
            }
            default -> throw new InvalidBudgetScopeException(
                    "Unsupported budget scope"
            );
        }
    }
}
