package bflow.income;

import bflow.income.entity.Income;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
}
