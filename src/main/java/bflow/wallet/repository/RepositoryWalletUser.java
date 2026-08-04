package bflow.wallet.repository;

import bflow.wallet.entities.WalletUser;
import bflow.wallet.enums.WalletRole;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryWalletUser extends JpaRepository<WalletUser, UUID>,
        JpaSpecificationExecutor<WalletUser> {
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

     /**
     * Counts the number of wallets where the user has the specified role.
     *
     * @param userId the user UUID
     * @param role the wallet role
     * @return the number of matching wallet memberships
     */
    long countByUserIdAndRole(UUID userId, WalletRole role);

    /**
     * Counts the total number of members in a wallet.
     *
     * @param walletId the wallet UUID
     * @return the total number of wallet members
     */
    long countByWalletId(UUID walletId);

    /**
     * Checks whether a user belongs to the specified wallet.
     *
     * @param walletId the wallet UUID
     * @param userId the user UUID
     * @return {@code true} if the user belongs to the wallet,
     * otherwise {@code false}
     */
    boolean existsByWalletIdAndUserId(
            UUID walletId,
            UUID userId
    );

    /**
     * Checks whether a user with the specified email belongs to the wallet.
     *
     * @param walletId the wallet UUID
     * @param email the user's email address
     * @return {@code true} if the user belongs to the wallet,
     * otherwise {@code false}
     */
    boolean existsByWalletIdAndUserEmail(
            UUID walletId,
            String email
    );

    /**
     * Finds a wallet membership by wallet ID and user email.
     *
     * @param walletId the wallet UUID
     * @param email the user's email address
     * @return an optional containing the wallet membership if found
     */
    Optional<WalletUser> findByWalletIdAndUserEmail(
            UUID walletId,
            String email
    );

    /**
     * Deletes the relationship between a wallet and a user.
     *
     * @param walletId the wallet UUID
     * @param userId the user UUID
     */
    void deleteByWalletIdAndUserId(
            UUID walletId,
            UUID userId
    );

    /**
     * Finds all members associated with the specified wallet.
     *
     * @param walletId the wallet UUID
     * @return a list of wallet members
     */
    List<WalletUser> findByWalletId(UUID walletId);

    @Query("SELECT wu.wallet.id FROM WalletUser wu WHERE wu.user.id = :userId")
    List<UUID> findWalletIdsByUserId(UUID userId);
}
