package bflow.category;

import bflow.category.entity.Category;
import bflow.category.enums.CategoryType;
import bflow.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Validator for category operations.
 * Provides reusable validation logic for financial transactions.
 */
@Component
public class CategoryValidator {

    /**
     * Validates that a category exists and is of type EXPENSE.
     * Used for expense transactions.
     *
     * @param category the category to validate
     * @throws ResourceNotFoundException if category is null
     * @throws IllegalArgumentException if category type is not EXPENSE
     */
    public void validateExpenseCategory(final Category category) {
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException(
                "Category must be of type EXPENSE, but got "
                + category.getType()
            );
        }
    }

    /**
     * Validates that a category exists and is of type INCOME.
     * Used for income transactions.
     *
     * @param category the category to validate
     * @throws ResourceNotFoundException if category is null
     * @throws IllegalArgumentException if category type is not INCOME
     */
    public void validateIncomeCategory(final Category category) {
        if (category == null) {
            throw new ResourceNotFoundException("Category not found");
        }

        if (category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException(
                "Category must be of type INCOME, but got " + category.getType()
            );
        }
    }
}
