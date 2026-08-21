package bflow.auth.security;

import bflow.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Entry point invoked by Spring Security whenever an unauthenticated
 * request is rejected.
 *
 * <p>Runs inside the security filter chain, before the
 * {@code DispatcherServlet}, which is why this — and not
 * {@code GlobalExceptionHandler}'s {@code @ExceptionHandler}
 * methods — is the correct place to handle authentication failures.
 * Exceptions thrown while decoding/validating the bearer token never
 * reach a controller, so a {@code @RestControllerAdvice} can never
 * see them.</p>
 *
 * <p>Distinguishes two very different situations that both surface
 * as {@link AuthenticationException}:</p>
 * <ul>
 *     <li>A genuinely invalid request (missing, malformed or
 *     expired token) — a normal, expected client-side condition.
 *     Logged at WARN, responds 401.</li>
 *     <li>{@link AuthenticationServiceException} — Spring Security
 *     could not even complete the authentication check, e.g.
 *     Cognito's JWKS endpoint timed out. This is an infrastructure
 *     problem, not a bad token. Logged at ERROR without a stack
 *     trace and throttled to at most one line per configured window
 *     (repeats are counted, not re-logged), so a sustained outage
 *     under real traffic doesn't flood CloudWatch with repeated,
 *     identical stack traces. Responds 503.</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /** JSON serializer for the error response body. */
    private final ObjectMapper objectMapper;

    /** Minimum gap between consecutive infra-failure ERROR logs. */
    @Value("${app.security.auth-error-log-throttle-seconds:60}")
    private long throttleSeconds;

    /** Timestamp of the last emitted infra-failure ERROR log. */
    private final AtomicReference<Instant> lastLoggedAt =
            new AtomicReference<>(Instant.EPOCH);

    /** Count of infra failures suppressed since the last log line. */
    private final AtomicInteger suppressedCount = new AtomicInteger(0);

    /**
     * {@inheritDoc}
     */
    @Override
    public void commence(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final AuthenticationException authException
    ) throws IOException {

        if (authException instanceof AuthenticationServiceException) {
            logInfraFailure(request, authException);

            writeError(
                    response,
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication is temporarily unavailable; "
                            + "please try again shortly",
                    request.getRequestURI()
            );
            return;
        }

        log.warn(
                "Unauthenticated request rejected for {} {}: {}",
                request.getMethod(), request.getRequestURI(),
                authException.getMessage()
        );

        writeError(
                response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required",
                request.getRequestURI()
        );
    }

    /**
     * Logs an infrastructure-level authentication failure (e.g. the
     * JWKS endpoint being unreachable), without a stack trace and
     * throttled to at most one line per {@link #throttleSeconds}, so
     * a sustained outage doesn't flood the logs with one identical
     * entry per failed request.
     *
     * @param request the rejected request
     * @param authException the underlying authentication exception
     */
    private void logInfraFailure(
            final HttpServletRequest request,
            final AuthenticationException authException
    ) {
        Instant now = Instant.now();
        Instant previous = lastLoggedAt.get();

        boolean dueToLog =
                Duration.between(previous, now).getSeconds() >= throttleSeconds
                        && lastLoggedAt.compareAndSet(previous, now);

        if (!dueToLog) {
            suppressedCount.incrementAndGet();
            return;
        }

        int suppressed = suppressedCount.getAndSet(0);

        log.error(
                "Authentication could not be completed for {} {}: {} "
                        + "({} similar failures suppressed in the "
                        + "last {}s)",
                request.getMethod(), request.getRequestURI(),
                authException.getMessage(), suppressed, throttleSeconds
        );
    }

    /**
     * Writes a JSON error response consistent with the application's
     * ApiResponse format.
     *
     * @param response HTTP response to write.
     * @param status HTTP status code.
     * @param message error message.
     * @param path request URI.
     * @throws IOException if writing the response fails.
     */
    private void writeError(
            final HttpServletResponse response,
            final int status,
            final String message,
            final String path
    ) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.error(message, path);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
