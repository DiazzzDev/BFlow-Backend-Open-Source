package bflow.budget;

import bflow.budget.entity.Budget;
import bflow.budget.enums.PeriodType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Budget entities.
 */
@Repository
public interface RepositoryBudget extends JpaRepository<Budget, UUID> {
    /**
     * Find a budget by wallet ID, user ID, and period.
     *
     * @param walletId the wallet ID
     * @param userId the user ID
     * @param period the period type
     * @return an Optional containing the budget if found
     */
    Optional<Budget> findByWalletIdAndUserIdAndPeriod(
            UUID walletId,
            UUID userId,
            PeriodType period
    );

    /**
     * Find all budgets for a specific wallet.
     *
     * @param walletId the wallet ID
     * @return list of budgets
     */
    List<Budget> findByWalletId(UUID walletId);
}
