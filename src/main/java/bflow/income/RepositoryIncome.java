package bflow.income;

import bflow.dashboard.projection.MonthlyTotalProjection;
import bflow.income.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RepositoryIncome extends JpaRepository<Income, UUID> {
    /**
    * Retrieves incomes belonging to a specific wallet.
    *
    * @param walletId the wallet identifier.
    * @param pageable pagination configuration.
    * @return a page containing wallet incomes.
    */
    Page<Income> findByWalletId(UUID walletId, Pageable pageable);

    /**
     * Counts the total number of incomes for a wallet.
     *
     * @param walletId wallet identifier.
     * @return total income count.
     */
    long countByWalletId(UUID walletId);

    /**
     * Retrieves the latest income creation timestamp for a wallet.
     *
     * @param walletId wallet identifier.
     * @return most recent creation timestamp, or {@code null} if no incomes
     *         exist.
     */
    @Query(
        "SELECT MAX(i.createdAt) FROM Income i WHERE i.wallet.id = :walletId"
    )
    Instant findMaxCreatedAtByWalletId(UUID walletId);

    /**
     * Sums incomes across a set of wallets within a date range.
     *
     * @param walletIds the wallet IDs to search
     * @param start range start (inclusive)
     * @param end range end (inclusive)
     * @return the sum of incomes
     */
    @Query("""
    SELECT COALESCE(SUM(i.amount), 0)
    FROM Income i
    WHERE i.wallet.id IN :walletIds
    AND i.date BETWEEN :start AND :end
""")
    BigDecimal sumByWalletsAndDateRange(
            List<UUID> walletIds, LocalDate start, LocalDate end);

    /**
     * Groups incomes by month within a date range.
     *
     * @param walletIds the wallet IDs to search
     * @param start range start (inclusive)
     * @param end range end (inclusive)
     * @return monthly income totals
     */
    @Query("""
    SELECT EXTRACT(MONTH FROM i.date) as month,
           COALESCE(SUM(i.amount), 0) as total
    FROM Income i
    WHERE i.wallet.id IN :walletIds
    AND i.date BETWEEN :start AND :end
    GROUP BY EXTRACT(MONTH FROM i.date)
""")
    List<MonthlyTotalProjection> sumGroupedByMonth(
            List<UUID> walletIds, LocalDate start, LocalDate end);

    /**
     * Retrieves the most recent incomes across a set of wallets.
     *
     * @param walletIds the wallet IDs to search
     * @param pageable pagination configuration
     * @return incomes ordered by creation date descending
     */
    List<Income> findByWalletIdInOrderByCreatedAtDesc(
            List<UUID> walletIds, Pageable pageable);
}
