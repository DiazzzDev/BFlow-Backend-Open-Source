package bflow.common.exception;

/**
 * Exception thrown when the storage provider (AWS S3) cannot
 * complete an upload, download, delete or existence check.
 */
public class StorageException extends RuntimeException {

    /**
     * Create a new storage failure exception.
     *
     * @param message error details
     */
    public StorageException(final String message) {
        super(message);
    }

    /**
     * Create a new storage failure exception with a cause.
     *
     * @param message error details
     * @param cause underlying exception
     */
    public StorageException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
