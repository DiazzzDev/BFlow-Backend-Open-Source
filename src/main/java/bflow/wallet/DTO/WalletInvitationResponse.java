package bflow.wallet.DTO;

import bflow.wallet.enums.WalletInvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response returned after creating or retrieving a wallet invitation.
 */
@Getter
@AllArgsConstructor
public final class WalletInvitationResponse {

    /** Invitation identifier. */
    private final UUID id;

    /** Wallet identifier. */
    private final UUID walletId;

    /** Invited email. */
    private final String invitedEmail;

    /** Current invitation status. */
    private final WalletInvitationStatus status;

    /** Invitation expiration date. */
    private final Instant expiresAt;
}