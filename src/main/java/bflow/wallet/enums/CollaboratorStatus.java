package bflow.wallet.enums;

/**
 * Status of a user relative to a wallet, from the perspective of the
 * collaborator search endpoint.
 */
public enum CollaboratorStatus {

    /** The user can be invited to the wallet. */
    INVITABLE,

    /** The user already belongs to the wallet. */
    ALREADY_MEMBER,

    /** The user already has a pending invitation for the wallet. */
    INVITATION_PENDING
}
