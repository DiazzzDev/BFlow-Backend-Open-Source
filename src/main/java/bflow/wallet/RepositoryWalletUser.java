package bflow.wallet;

import bflow.wallet.entities.WalletUser;
import bflow.wallet.enums.WalletRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryWalletUser extends JpaRepository<WalletUser, UUID> {
    /**
     * Finds a wallet-user relationship by wallet ID and user ID.
     * @param walletId the wallet UUID.
     * @param userId the user UUID.
     * @return optional wallet-user relationship.
     */
    Optional<WalletUser> findByWalletIdAndUserId(UUID walletId, UUID userId);

    /**
     * Finds wallet-user relationships by user ID with pagination.
     * @param userId the user UUID.
     * @param pageable the pagination information.
     * @return a page of wallet-user relationships.
     */
    Page<WalletUser> findByUserId(UUID userId, Pageable pageable);

    /**
     * Find the first wallet-user relationship for a user with a specific role.
     *
     * @param userId the user UUID
     * @param role the wallet role
     * @return optional wallet-user relationship
     */
    Optional<WalletUser> findFirstByUserIdAndRole(
            UUID userId,
            WalletRole role
    );

    /**
     * Checks whether the user belongs to at least one wallet.
     *
     * @param userId user identifier
     * @return true if a wallet association exists
     */
    boolean existsByUserId(UUID userId);

    long countByUserIdAndRole(UUID userId, WalletRole role);

    long countByWalletId(UUID walletId);

    boolean existsByWalletIdAndUserId(
            UUID walletId,
            UUID userId
    );

    boolean existsByWalletIdAndUserEmail(
            UUID walletId,
            String email
    );

    Optional<WalletUser> findByWalletIdAndUserEmail(
            UUID walletId,
            String email
    );

    void deleteByWalletIdAndUserId(
            UUID walletId,
            UUID userId
    );

    List<WalletUser> findByWalletId(UUID walletId);
}
