package bflow.expenses;

import bflow.expenses.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.Collection;
import java.util.List;

@Repository
public interface RepositoryExpense extends JpaRepository<Expense, UUID> {
    /**
     * Retrieves only the values needed to calculate a page of budgets.
     * This allows budget results to be calculated with one aggregate source
     * query instead of one query per budget.
     *
     * @param walletIds wallets represented in the budget page
     * @param categoryIds categories represented in the budget page
     * @param start earliest budget start date
     * @param end latest budget end date
     * @return lightweight expense data for page calculations
     */
    @Query("""
        SELECT e.wallet.id AS walletId, e.category.id AS categoryId,
               e.date AS date, e.amount AS amount
        FROM Expense e
        WHERE (e.wallet.id IN :walletIds OR e.category.id IN :categoryIds)
        AND e.date BETWEEN :start AND :end
    """)
    List<BudgetExpenseData> findBudgetExpenseData(
            Collection<UUID> walletIds,
            Collection<UUID> categoryIds,
            LocalDate start,
            LocalDate end
    );
    /**
    * Retrieves expenses belonging to a specific wallet.
    *
    * @param walletId the wallet identifier.
    * @param pageable pagination configuration.
    * @return a page containing wallet expenses.
    */
    Page<Expense> findByWalletId(UUID walletId, Pageable pageable);

    /**
     * Sum expenses for a wallet within a date range.
     *
     * @param walletId the wallet ID
     * @param start the start date
     * @param end the end date
     * @return the sum of expenses
     */
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.wallet.id = :walletId
        AND e.date BETWEEN :start AND :end
    """)
    BigDecimal sumExpensesByWalletAndDateRange(
            UUID walletId,
            LocalDate start,
            LocalDate end
    );

    /**
     * Sum expenses for a category within a date range.
     *
     * @param categoryId the category ID
     * @param start the start date
     * @param end the end date
     * @return the sum of expenses
     */
    @Query("""
        SELECT COALESCE(SUM(e.amount), 0)
        FROM Expense e
        WHERE e.category.id = :categoryId
        AND e.date BETWEEN :start AND :end
    """)
    BigDecimal sumByCategoryAndDateRange(
            UUID categoryId,
            LocalDate start,
            LocalDate end
    );
}
