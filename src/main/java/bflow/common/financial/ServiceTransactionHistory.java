package bflow.common.financial;

import bflow.auth.services.UserServiceImpl;
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

    private final RepositoryTransactionHistory repositoryTransactionHistory;
    private final RepositoryWalletUser repositoryWalletUser;
    private final UserServiceImpl userService;

    /**
     * Unified transaction history across every wallet the user belongs to.
     */
    public Page<TransactionResponse> getGlobalHistory(
            final UUID userId, final String query,
            final TransactionType type, final Pageable pageable
    ) {
        userService.validateUserActive(userId);
        List<UUID> walletIds = repositoryWalletUser.findWalletIdsByUserId(userId);

        return repositoryTransactionHistory.search(
                walletIds,
                TransactionSearchCriteria.global(query, type),
                pageable
        );
    }

    /**
     * Unified transaction history scoped to a single wallet.
     * @throws AccessDeniedException if the user has no access to the wallet.
     */
    public Page<TransactionResponse> getWalletHistory(
            final UUID walletId, final UUID userId, final String query,
            final TransactionType type, final Pageable pageable
    ) {
        userService.validateUserActive(userId);

        repositoryWalletUser.findByWalletIdAndUserId(walletId, userId)
                .orElseThrow(() -> new AccessDeniedException(
                        "User does not have access to this wallet"
                ));

        return repositoryTransactionHistory.search(
                List.of(walletId),
                TransactionSearchCriteria.forWallet(query, walletId, type),
                pageable
        );
    }
}
