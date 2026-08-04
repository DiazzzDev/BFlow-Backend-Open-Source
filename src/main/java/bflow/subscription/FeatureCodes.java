package bflow.subscription;

/**
 * Defines feature codes used by the subscription system.
 */
public final class FeatureCodes {

    /**
     * Budget management feature.
     */
    public static final String BUDGETS = "BUDGETS";

    /**
     * Wallet management feature.
     */
    public static final String WALLETS = "WALLETS";

    /**
     * Shared wallet feature.
     */
    public static final String SHARED_WALLETS = "SHARED_WALLETS";

    /**
     * Shared wallet members feature.
     */
    public static final String WALLET_MEMBERS = "WALLET_MEMBERS";

    /**
     * Recurring transactions feature.
     */
    public static final String RECURRING_TRANSACTIONS =
            "RECURRING_TRANSACTIONS";

    /**
     * Dashboard customization feature.
     */
    public static final String DASHBOARD_CUSTOMIZATION =
            "DASHBOARD_CUSTOMIZATION";

    /**
     * Data export feature.
     */
    public static final String EXPORT = "EXPORT";

    /**
     * Permission to create shared wallets.
     */
    public static final String CAN_CREATE_SHARED_WALLETS =
            "CAN_CREATE_SHARED_WALLETS";

    /**
     * Prevents instantiation.
     */
    private FeatureCodes() {
    }
}
