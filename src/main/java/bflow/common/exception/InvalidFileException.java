package bflow.common.exception;

/**
 * Exception thrown when a file does not satisfy the application's
 * upload constraints (size or content type).
 */
public class InvalidFileException extends IllegalStateException {

    /**
     * Construct an InvalidFileException with a message.
     *
     * @param message the exception message
     */
    public InvalidFileException(final String message) {
        super(message);
    }
}
