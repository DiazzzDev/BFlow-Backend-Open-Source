package bflow.wallet.DTO;

import bflow.wallet.enums.WalletInvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned when listing invitations a user has sent.
 *
 * <p>Unlike {@link WalletInvitationResponse} — used right after an
 * invitation is created, when the wallet and sender are already
 * known to the caller — this carries the wallet's name and, when the
 * invited email already belongs to a registered account, that
 * user's id and name, so the sender can review their sent
 * invitations without resolving those separately.</p>
 */
@Getter
@AllArgsConstructor
public final class WalletInvitationSentResponse {

    /** Invitation identifier. */
    private final UUID id;

    /** Wallet identifier. */
    private final UUID walletId;

    /** Wallet name, at the time of this response. */
    private final String walletName;

    /** Invited email. */
    private final String invitedEmail;

    /**
     * Identifier of the account matching {@code invitedEmail}, or
     * {@code null} if that email hasn't registered yet.
     */
    private final UUID invitedUserId;

    /**
     * Name of the account matching {@code invitedEmail}, or {@code
     * null} if that email hasn't registered yet.
     */
    private final String invitedUserName;

    /** Current invitation status. */
    private final WalletInvitationStatus status;

    /** Invitation creation date. */
    private final Instant createdAt;

    /** Invitation expiration date. */
    private final Instant expiresAt;

    /**
     * Date the invitation was accepted, rejected, or canceled;
     * {@code null} while still {@code PENDING}.
     */
    private final Instant respondedAt;
}
