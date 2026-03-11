package bflow.common.exception;

/**
 * Exception thrown when authentication fails due to incorrect credentials.
 */
public class InvalidCredentialsException extends RuntimeException {
    /**
     * Constructs a new exception with a default error message.
     */
    public InvalidCredentialsException() {
        super("Invalid credentials");
    }
}
