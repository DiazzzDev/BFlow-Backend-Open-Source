package bflow.common.financial;

import bflow.auth.services.UserService;
import bflow.wallet.repository.RepositoryWalletUser;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ServiceTransactionHistory {

    /**
     * Repository used to retrieve unified transaction history.
     */
    private final RepositoryTransactionHistory repositoryTransactionHistory;

    /**
     * Repository used to retrieve wallet memberships.
     */
    private final RepositoryWalletUser repositoryWalletUser;

    /**
     * Service used to validate user status.
     */
    private final UserService userService;

    /**
     * Retrieves the unified transaction history across every wallet the user
     * belongs to.
     *
     * @param userId authenticated user identifier.
     * @param query optional search text.
     * @param type optional transaction type filter.
     * @param contributorIds optional contributor id filter.
     * @param pageable pagination information.
     * @return paginated transaction history.
     */
    public Page<TransactionResponse> getGlobalHistory(
            final UUID userId,
            final String query,
            final TransactionType type,
            final List<UUID> contributorIds,
            final Pageable pageable
    ) {
        userService.validateUserActive(userId);

        List<UUID> walletIds = repositoryWalletUser.findWalletIdsByUserId(
                userId
        );

        return repositoryTransactionHistory.search(
                walletIds,
                TransactionSearchCriteria.global(query, type, contributorIds),
                pageable
        );
    }

    /**
     * Retrieves the transaction history for a specific wallet.
     *
     * @param walletId wallet identifier.
     * @param userId authenticated user identifier.
     * @param query optional search text.
     * @param type optional transaction type filter.
     * @param contributorIds optional contributor id filter.
     * @param pageable pagination information.
     * @return paginated wallet transaction history.
     * @throws AccessDeniedException if the user has no access to the wallet.
     */
    public Page<TransactionResponse> getWalletHistory(
            final UUID walletId,
            final UUID userId,
            final String query,
            final TransactionType type,
            final List<UUID> contributorIds,
            final Pageable pageable
    ) {
        userService.validateUserActive(userId);

        repositoryWalletUser.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "User does not have access to this wallet"
                ));

        return repositoryTransactionHistory.search(
                List.of(walletId),
                TransactionSearchCriteria.forWallet(
                        query, walletId, type, contributorIds
                ),
                pageable
        );
    }
}
