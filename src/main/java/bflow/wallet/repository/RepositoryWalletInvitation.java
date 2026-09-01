package bflow.wallet.repository;

import bflow.wallet.entities.WalletInvitation;
import bflow.wallet.enums.WalletInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for wallet invitations.
 */
@Repository
public interface RepositoryWalletInvitation
        extends JpaRepository<WalletInvitation, UUID> {

    /**
     * Finds an invitation by its public token.
     *
     * @param token invitation token
     * @return optional invitation
     */
    Optional<WalletInvitation> findByToken(String token);

    /**
     * Counts invitations for a wallet with a given status.
     *
     * @param walletId wallet identifier
     * @param status invitation status
     * @return number of invitations
     */
    long countByWalletIdAndStatus(
            UUID walletId,
            WalletInvitationStatus status
    );

    /**
     * Returns pending invitations for an email, eagerly fetching the
     * wallet and inviting user so the response can be built without
     * triggering a lazy-load query per invitation.
     *
     * @param invitedEmail invited email
     * @param status invitation status
     * @return invitation list
     */
    @Query(
            "SELECT wi FROM WalletInvitation wi "
                    + "JOIN FETCH wi.wallet "
                    + "JOIN FETCH wi.invitedByUser "
                    + "WHERE wi.invitedEmail = :invitedEmail "
                    + "AND wi.status = :status"
    )
    List<WalletInvitation> findByInvitedEmailAndStatus(
            String invitedEmail,
            WalletInvitationStatus status
    );

    /**
     * Checks whether an invitation already exists for the specified wallet,
     * email address, and invitation status.
     *
     * @param walletId the wallet UUID
     * @param invitedEmail the invited user's email address
     * @param status the invitation status
     * @return {@code true} if a matching invitation exists;
     * otherwise {@code false}
     */
    boolean existsByWalletIdAndInvitedEmailAndStatus(
            UUID walletId,
            String invitedEmail,
            WalletInvitationStatus status
    );

    /**
     * Returns every invitation sent by a given user, across all of
     * their wallets and regardless of status, newest first.
     *
     * @param invitedByUserId identifier of the user who sent the
     *         invitations
     * @return the sender's full invitation history
     */
    List<WalletInvitation> findByInvitedByUserIdOrderByCreatedAtDesc(
            UUID invitedByUserId
    );

    /**
     * Returns every invitation sent by a given user for a specific
     * wallet, regardless of status, newest first.
     *
     * @param invitedByUserId identifier of the user who sent the
     *         invitations
     * @param walletId the wallet UUID to filter by
     * @return the sender's invitation history for that wallet
     */
    List<WalletInvitation> findByInvitedByUserIdAndWalletIdOrderByCreatedAtDesc(
            UUID invitedByUserId,
            UUID walletId
    );

    /**
     * Retrieves the email addresses with a pending invitation for
     * the specified wallet. Used to resolve collaborator search
     * status in bulk instead of querying per candidate.
     *
     * @param walletId the wallet UUID
     * @param status the invitation status
     * @return the invited email addresses
     */
    @Query(
            "SELECT wi.invitedEmail FROM WalletInvitation wi "
                    + "WHERE wi.wallet.id = :walletId AND wi.status = :status"
    )
    List<String> findInvitedEmailsByWalletIdAndStatus(
            UUID walletId,
            WalletInvitationStatus status
    );
}
