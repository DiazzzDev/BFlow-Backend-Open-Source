package bflow.notifications.enums;

/**
 * Notification type enumeration.
 */
public enum NotificationType {
    /**
     * Budget success notification.
     */
    BUDGET_SUCCESS,
    /**
     * Budget warning notification.
     */
    BUDGET_WARNING,
    /**
     * Budget critical notification.
     */
    BUDGET_CRITICAL,
    /**
     * Budget exceeded notification.
     */
    BUDGET_EXCEEDED,
    /**
     * Goal reached notification (Future feature).
     */
    GOAL_REACHED,
    /**
     * New contributor notification (Future feature).
     */
    NEW_CONTRIBUTOR,
    /**
     * Account locked notification.
     */
    ACCOUNT_LOCKED
}
