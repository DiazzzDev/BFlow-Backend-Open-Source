package bflow.wallet.repository;

import bflow.wallet.entities.WalletInvitation;
import bflow.wallet.enums.WalletInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Finds a pending invitation for the same wallet and email.
     *
     * @param walletId wallet identifier
     * @param invitedEmail invited email
     * @param status invitation status
     * @return optional invitation
     */
    Optional<WalletInvitation> findByWalletIdAndInvitedEmailAndStatus(
            UUID walletId,
            String invitedEmail,
            WalletInvitationStatus status
    );

    /**
     * Returns pending invitations for an email.
     *
     * @param invitedEmail invited email
     * @param status invitation status
     * @return invitation list
     */
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
}
