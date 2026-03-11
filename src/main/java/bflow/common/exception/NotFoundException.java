package bflow.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when a requested resource is not found.
 * Maps to HTTP 404 Not Found response status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {
    /**
     * Constructs a NotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public NotFoundException(final String message) {
        super(message);
    }
}
