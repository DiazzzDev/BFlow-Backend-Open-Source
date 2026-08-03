package bflow.wallet.enums;

/**
 * Represents the lifecycle of a wallet invitation.
 */
public enum WalletInvitationStatus {

    /**
     * The invitation has been created and is awaiting a response.
     */
    PENDING,

    /**
     * The invitation has been accepted by the recipient.
     */
    ACCEPTED,

    /**
     * The invitation has been declined by the recipient.
     */
    REJECTED,

    /**
     * The invitation expired before the recipient responded.
     */
    EXPIRED,

    /**
     * The invitation was canceled by the wallet owner.
     */
    CANCELED
}
