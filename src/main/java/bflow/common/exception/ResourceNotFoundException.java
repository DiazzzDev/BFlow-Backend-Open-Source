package bflow.common.exception;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found response status.
 * This includes Expense, Income, Wallet, and similar resources.
 *
 * Usage example:
 * <pre>
 *   throw new ResourceNotFoundException("Expense not found");
 * </pre>
 */
public class ResourceNotFoundException extends NotFoundException {
    /**
     * Constructs a ResourceNotFoundException with detail message.
     *
     * @param message the detail message describing which resource
     *                was not found
     */
    public ResourceNotFoundException(final String message) {
        super(message);
    }

    /**
     * Constructs a ResourceNotFoundException with message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public ResourceNotFoundException(final String message,
                                      final Throwable cause) {
        super(message);
        initCause(cause);
    }
}
