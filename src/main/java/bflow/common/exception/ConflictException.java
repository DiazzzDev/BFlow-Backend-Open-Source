package bflow.common.exception;

/**
 * Exception thrown when a request conflicts with the current
 * state of a resource.
 */
public class ConflictException extends RuntimeException {

    /**
     * Creates a new conflict exception.
     *
     * @param message the exception message
     */
    public ConflictException(final String message) {
        super(message);
    }

}
