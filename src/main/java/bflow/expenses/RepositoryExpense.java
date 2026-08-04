package bflow.expenses;

import bflow.expenses.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryExpense extends JpaRepository<Expense, UUID> {
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

    /**
     * Retrieves the expense with the highest amount for a wallet.
     *
     * @param walletId wallet identifier.
     * @return the highest expense, if one exists.
     */
    Optional<Expense> findTopByWalletIdOrderByAmountDesc(UUID walletId);

    /**
     * Counts the total number of expenses for a wallet.
     *
     * @param walletId wallet identifier.
     * @return total expense count.
     */
    long countByWalletId(UUID walletId);

    /**
     * Retrieves the latest expense creation timestamp for a wallet.
     *
     * @param walletId wallet identifier.
     * @return most recent creation timestamp, or {@code null} if no expenses
     *         exist.
     */
    @Query(
        "SELECT MAX(e.createdAt) FROM Expense e WHERE e.wallet.id = :walletId"
    )
    Instant findMaxCreatedAtByWalletId(UUID walletId);
}
