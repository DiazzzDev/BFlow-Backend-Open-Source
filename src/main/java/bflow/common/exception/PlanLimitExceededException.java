package bflow.common.exception;

/**
 * Exception thrown when a user exceeds the limits imposed
 * by their current subscription plan.
 */
public class PlanLimitExceededException extends RuntimeException {

    /**
     * Creates a new plan limit exceeded exception.
     *
     * @param message the exception message
     */
    public PlanLimitExceededException(final String message) {
        super(message);
    }
}
