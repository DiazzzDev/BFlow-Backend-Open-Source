package bflow.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a user attempts to access a stored file
 * that does not belong to them.
 * Maps to HTTP 403 Forbidden response status.
 *
 * Usage example:
 * <pre>
 *   throw new FileAccessDeniedException(
 *     "User does not have access to this file"
 *   );
 * </pre>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class FileAccessDeniedException extends RuntimeException {
    /**
     * Constructs a FileAccessDeniedException with detail message.
     *
     * @param message the detail message describing why access
     *                was denied
     */
    public FileAccessDeniedException(final String message) {
        super(message);
    }
}
