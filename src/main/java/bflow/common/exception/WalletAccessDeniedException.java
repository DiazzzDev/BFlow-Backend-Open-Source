package bflow.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when user attempts to access unauthorized resource.
 * Maps to HTTP 403 Forbidden response status.
 * Typically occurs when a user tries to access another user's wallet
 * or performs operations they don't have permission for.
 *
 * Usage example:
 * <pre>
 *   throw new WalletAccessDeniedException(
 *     "User does not have access to this wallet"
 *   );
 * </pre>
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class WalletAccessDeniedException extends RuntimeException {
    /**
     * Constructs a WalletAccessDeniedException with detail message.
     *
     * @param message the detail message describing why access
     *                was denied
     */
    public WalletAccessDeniedException(final String message) {
        super(message);
    }

    /**
     * Constructs a WalletAccessDeniedException with message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public WalletAccessDeniedException(final String message,
                                        final Throwable cause) {
        super(message, cause);
    }
}
