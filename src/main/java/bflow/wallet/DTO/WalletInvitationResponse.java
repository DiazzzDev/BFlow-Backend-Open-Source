package bflow.wallet.DTO;

import bflow.wallet.enums.WalletInvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned after creating or retrieving a wallet
 * invitation. Carries enough of the wallet and inviter's public
 * profile for a "pending invitations" screen to render fully
 * (name, email, avatar) without a follow-up request per invitation.
 */
@Getter
@AllArgsConstructor
public final class WalletInvitationResponse {

    /** Invitation identifier. */
    private final UUID id;

    /** Wallet identifier. */
    private final UUID walletId;

    /** Name of the wallet being shared. */
    private final String walletName;

    /** Invited email. */
    private final String invitedEmail;

    /** Display name of the user who sent the invitation. */
    private final String invitedByName;

    /** Email of the user who sent the invitation. */
    private final String invitedByEmail;

    /** Profile picture URL of the user who sent the invitation. */
    private final String invitedByPictureUrl;

    /** Current invitation status. */
    private final WalletInvitationStatus status;

    /** Invitation expiration date. */
    private final Instant expiresAt;
}
