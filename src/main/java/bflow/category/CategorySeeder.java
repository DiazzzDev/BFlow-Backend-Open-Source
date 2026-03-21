package bflow.category;

import bflow.category.entity.Category;
import bflow.category.enums.CategoryType;
import bflow.expenses.enums.ExpenseType;
import bflow.income.enums.IncomeType;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Component responsible for seeding default system-defined categories.
 * Initializes the database with predefined expense and income categories on
 * application startup.
 */
@Component
@RequiredArgsConstructor
public class CategorySeeder {

    /**
     * Repository for category database operations.
     */
    private final RepositoryCategory repositoryCategory;

    /**
     * Seeds the database with default categories during application startup.
     * Creates system-defined categories for all expense and income types if
     * the database is empty.
     */
    @PostConstruct
    public void seed() {

        if (repositoryCategory.count() > 0) {
            return;
        }

        for (ExpenseType type : ExpenseType.values()) {

            Category category = new Category();
            category.setName(type.name());
            category.setType(CategoryType.EXPENSE);
            category.setSystemDefined(true);

            repositoryCategory.save(category);
        }

        for (IncomeType type : IncomeType.values()) {

            Category category = new Category();
            category.setName(type.name());
            category.setType(CategoryType.INCOME);
            category.setSystemDefined(true);

            repositoryCategory.save(category);
        }

    }

}
