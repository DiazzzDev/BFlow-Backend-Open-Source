package bflow.wallet.enums;

/**
 * Filter scope for the wallet listing endpoint, based on how many
 * members a wallet has — independent of the caller's role (OWNER/MEMBER).
 */
public enum WalletScope {
    /** Wallets where the caller is the only member. */
    MINE,
    /** Wallets with more than one member (shared, regardless of role). */
    SHARED
}
