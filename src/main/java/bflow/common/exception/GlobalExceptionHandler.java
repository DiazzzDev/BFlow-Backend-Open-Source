package bflow.common.exception;

import bflow.common.response.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.converter.HttpMessageNotReadableException;

import tools.jackson.databind.exc.InvalidFormatException;

/**
 * Global controller advice to handle application-wide exceptions.
 */
@Slf4j
@RestControllerAdvice
public final class GlobalExceptionHandler {

    /**
     * Handles IllegalStateExceptions (e.g., conflicts).
     * @param ex the exception.
     * @param request the current request.
     * @return error response with CONFLICT status.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(
            final IllegalStateException ex,
            final HttpServletRequest request) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), request.getRequestURI()));
    }

    /**
     * Handles authentication credential failures.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with UNAUTHORIZED status.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(
            final InvalidCredentialsException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles resource not found exceptions.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with NOT_FOUND status.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            final NotFoundException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles IllegalArgumentExceptions that represent missing resources.
     * Treated as 404 Not Found per convention.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with NOT_FOUND status.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            final IllegalArgumentException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles access denied exceptions (permission violations).
     * @param ex the exception.
     * @param request the current request.
     * @return error response with FORBIDDEN status.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            final AccessDeniedException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ex.getMessage() != null
                            ? ex.getMessage()
                            : "Access denied",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles wallet access denied exceptions (wallet-specific permission
     * violations).
     * @param ex the exception.
     * @param request the current request.
     * @return error response with FORBIDDEN status.
     */
    @ExceptionHandler(WalletAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleWalletAccessDenied(
            final WalletAccessDeniedException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles bean validation errors.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with BAD_REQUEST status.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            final MethodArgumentNotValidException ex,
            final HttpServletRequest request) {
        String errorMsg = ex.getBindingResult().getFieldErrors()
            .stream()
            .map(err -> err.getField() + ": "
                + err.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(errorMsg, request.getRequestURI()));
    }

    /**
     * Handles client disconnects and aborted HTTP connections.
     * These are common in production and should not be treated
     * as server-side failures.
     *
     * @param ex the exception.
     * @param request the current request.
     */
    @ExceptionHandler({
            ClientAbortException.class,
            AsyncRequestNotUsableException.class
    })
    public void handleClientDisconnect(
            final Exception ex,
            final HttpServletRequest request
    ) {

        log.warn(
                "CLIENT DISCONNECTED at {} {} - {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName()
        );
    }

    /**
     * Final fallback for unhandled exceptions.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with INTERNAL_SERVER_ERROR status.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(
            final Exception ex,
            final HttpServletRequest request
    ) {

        if (isIgnorableException(ex)) {

            log.warn(
                    "IGNORED NETWORK EXCEPTION at {} {} - {}",
                    request.getMethod(),
                    request.getRequestURI(),
                    ex.getClass().getSimpleName()
            );

            return null;
        }

        log.error(
                "UNHANDLED EXCEPTION at {} {} - {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex
        );

        ApiResponse<?> response = ApiResponse.error(
                "Internal server error",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    /**
     * Handles 404 not found exceptions.
     * @param ex the exception.
     * @param request the current request.
     * @return error response with NOT_FOUND status.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandlerFound(
            final NoHandlerFoundException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(
                        "Endpoint not found",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles HTTP method not supported exceptions.
     * @param request the current request.
     * @return error response with METHOD_NOT_ALLOWED status.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(
                        "Method not allowed",
                        request.getRequestURI()
                ));
    }

    /**
     * Handle invalid budget threshold and scope exceptions.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return response with BAD_REQUEST status
     */
    @ExceptionHandler({
            InvalidBudgetThresholdException.class,
            InvalidBudgetScopeException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            final RuntimeException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handle budget overlap exceptions.
     *
     * @param ex the exception
     * @param request the HTTP request
     * @return response with CONFLICT status
     */
    @ExceptionHandler(BudgetOverlapException.class)
    public ResponseEntity<ApiResponse<Void>> handleBudgetOverlap(
            final BudgetOverlapException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

        /**
         * Handle errors related to email delivery and return a service
         * unavailable response.
         *
         * @param ex the email delivery exception
         * @param request the HTTP request
         * @return a service unavailable response entity
         */
    @ExceptionHandler(EmailDeliveryException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailDeliveryException(
            final EmailDeliveryException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(
                        ApiResponse.error(
                                ex.getMessage(),
                                request.getRequestURI()
                        )
                );
    }

    /**
     * Determines whether the exception is a harmless
     * network/client disconnect exception.
     *
     * @param ex the exception.
     * @return true if ignorable.
     */
    private boolean isIgnorableException(final Throwable ex) {

        Throwable current = ex;

        while (current != null) {

            if (current instanceof ClientAbortException
                    || current instanceof AsyncRequestNotUsableException) {
                return true;
            }

            String message = current.getMessage();

            if (message != null
                    && (
                    message.contains("Broken pipe")
                            || message.contains("Connection reset by peer")
            )) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }

    /**
     * Handles JPA entity not found exceptions.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with NOT_FOUND status
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
        final EntityNotFoundException ex,
        final HttpServletRequest request
    ) {
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error(
            ex.getMessage(),
            request.getRequestURI()
        ));
    }

    /**
     * Handles errors communicating with external services.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_GATEWAY status
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ApiResponse<Void>> handleRestClientException(
        final RestClientException ex,
        final HttpServletRequest request
    ) {
        log.error("Error comunicándose con Wompi", ex);
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ApiResponse.error(
                "No fue posible comunicarse con el proveedor de pagos.",
                request.getRequestURI()
        ));
    }

    /**
     * Handles malformed or unreadable HTTP request bodies.
     *
     * @param request the current HTTP request
     * @param ex the exception
     * @return an error response with BAD_REQUEST status
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleHttpMessageNotReadable(
            final HttpServletRequest request,
            final HttpMessageNotReadableException ex
    ) {

        String message = "El cuerpo de la solicitud es inválido.";

        Throwable cause = ex.getMostSpecificCause();

        if (cause instanceof InvalidFormatException invalidFormat) {

            String field = invalidFormat.getPath()
                    .stream()
                    .findFirst()
                    .map(ref -> ref.getPropertyName())
                    .orElse("desconocido");

            if (invalidFormat.getTargetType() == UUID.class) {
                message = "El campo '%s' debe ser un UUID válido."
                        .formatted(field);
            } else {
                message = "El campo '%s' tiene un formato inválido."
                        .formatted(field);
            }
        }

        return ApiResponse.error(
                message,
                request.getRequestURI()
        );
    }

    /**
     * Handles subscription plan limit violations.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with PAYMENT_REQUIRED status
     */
    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePlanLimitExceeded(
            final PlanLimitExceededException ex,
            final HttpServletRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.PAYMENT_REQUIRED)
                .body(ApiResponse.error(
                        ex.getMessage(), request.getRequestURI()
                ));
    }

    /**
     * Handles database inconsistency errors caused by unexpected query results.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with CONFLICT status
     */
    @ExceptionHandler(IncorrectResultSizeDataAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleIncorrectResultSize(
            final IncorrectResultSizeDataAccessException ex,
            final HttpServletRequest request
    ) {

        log.error(
                "Database inconsistency at {}",
                request.getRequestURI(),
                ex
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "A data consistency problem was detected.",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles business conflict exceptions.
     *
     * @param ex the exception.
     * @param request the current request.
     * @return error response with CONFLICT status.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
        final ConflictException ex,
        final HttpServletRequest request
    ) {

        log.warn(
                "BUSINESS CONFLICT at {} {} - {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        ex.getMessage(),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles invalid request parameter values.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatch(
        final MethodArgumentTypeMismatchException ex,
        final HttpServletRequest request
    ) {

        String message = "Invalid value for parameter '%s'."
            .formatted(ex.getName());

        if (ex.getRequiredType() != null
            && ex.getRequiredType().isEnum()) {

            Object[] values = ex.getRequiredType().getEnumConstants();

            String allowedValues = java.util.Arrays.stream(values)
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(", "));

            message = "Invalid value '%s' for "
            + "parameter '%s'. Allowed values: %s."
                .formatted(
                        ex.getValue(),
                        ex.getName(),
                        allowedValues
                );
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(
                    message,
                    request.getRequestURI()
            ));
    }

    /**
     * Handles missing required request parameters.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestParameter(
            final MissingServletRequestParameterException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Missing required parameter '%s'."
                                .formatted(ex.getParameterName()),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles missing required request headers.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingRequestHeader(
            final MissingRequestHeaderException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Missing required header '%s'."
                                .formatted(ex.getHeaderName()),
                        request.getRequestURI()
                ));
    }

    /**
     * Handles method parameter validation failures.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleHandlerMethodValidation(
            final HandlerMethodValidationException ex,
            final HttpServletRequest request
    ) {

        String message = ex.getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        message.isBlank()
                                ? "Request validation failed."
                                : message,
                        request.getRequestURI()
                ));
    }

    /**
     * Handles constraint validation failures.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            final ConstraintViolationException ex,
            final HttpServletRequest request
    ) {

        String message = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        message,
                        request.getRequestURI()
                ));
    }

    /**
     * Handles unsupported media types.
     *
     * @param request the current HTTP request
     * @return a response with UNSUPPORTED_MEDIA_TYPE status
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(
                        "Unsupported media type.",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles unacceptable response media types.
     *
     * @param request the current HTTP request
     * @return a response with NOT_ACCEPTABLE status
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotAcceptable(
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiResponse.error(
                        "Requested media type is not supported.",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles database integrity constraint violations.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with CONFLICT status
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            final DataIntegrityViolationException ex,
            final HttpServletRequest request
    ) {

        log.warn(
                "DATABASE CONFLICT at {} {}",
                request.getMethod(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "The operation violates a database constraint.",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles optimistic locking failures.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with CONFLICT status
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailure(
            final OptimisticLockingFailureException ex,
            final HttpServletRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(
                        "The resource was modified by another request.",
                        request.getRequestURI()
                ));
    }

    /**
     * Handles binding errors.
     *
     * @param ex the exception
     * @param request the current HTTP request
     * @return a response with BAD_REQUEST status
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(
            final BindException ex,
            final HttpServletRequest request
    ) {

        String errorMsg = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        errorMsg,
                        request.getRequestURI()
                ));
    }
}
