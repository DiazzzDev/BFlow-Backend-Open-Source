package bflow.common.exception;

/**
 * Exception thrown when a storage object key is malformed or does
 * not belong to the application's trusted key namespace
 * ({@code users/{userId}/...} or {@code tmp/{uuid}}).
 *
 * <p>Object keys must never be trusted as-is when they originate
 * from client input.</p>
 */
public class InvalidStorageKeyException extends IllegalStateException {

    /**
     * Construct an InvalidStorageKeyException with a message.
     *
     * @param message the exception message
     */
    public InvalidStorageKeyException(final String message) {
        super(message);
    }
}
