package bflow.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import java.time.Instant;

/**
 * A generic wrapper for all API responses.
 * @param <T> the type of the data payload.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

    /** Indicates if the operation was successful. */
    private final boolean success;
    /** The response message. */
    private final String message;
    /** The data payload. */
    private final T data;
    /** The time the response was generated. */
    private final Instant timestamp;
    /** The request path. */
    private final String path;

    /**
     * Private constructor for ApiResponse.
     * @param aSuccess indicates success.
     * @param aMessage description message.
     * @param aData data payload.
     * @param aTimestamp creation time.
     * @param aPath request path.
     */
    private ApiResponse(
            final boolean aSuccess,
            final String aMessage,
            final T aData,
            final Instant aTimestamp,
            final String aPath
    ) {
        this.success = aSuccess;
        this.message = aMessage;
        this.data = aData;
        this.timestamp = aTimestamp;
        this.path = aPath;
    }

    /**
     * Creates a success response.
     * @param <T> the data type.
     * @param message the success message.
     * @param data the payload.
     * @param path the request path.
     * @return a success ApiResponse.
     */
    public static <T> ApiResponse<T> success(
            final String message,
            final T data,
            final String path
    ) {
        return new ApiResponse<>(
                true,
                message,
                data,
                Instant.now(),
                path
        );
    }

    /**
     * Creates an error response.
     * @param <T> the data type.
     * @param message the error message.
     * @param path the request path.
     * @return an error ApiResponse.
     */
    public static <T> ApiResponse<T> error(
            final String message,
            final String path
    ) {
        return new ApiResponse<>(
                false,
                message,
                null,
                Instant.now(),
                path
        );
    }
}
