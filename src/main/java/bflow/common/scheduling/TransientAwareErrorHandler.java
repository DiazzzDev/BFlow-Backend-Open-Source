package bflow.common.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.ErrorHandler;

import java.sql.SQLException;
import java.sql.SQLTransientException;

/**
 * Replaces Spring's default scheduled-task error handler
 * (TaskUtils$LoggingErrorHandler), which logs a full ERROR-level
 * stack trace on every single failure with no throttling or
 * classification.
 *
 * <p>For transient, infrastructure-level failures — a connection
 * pool blip, a pooler silently dropping an idle connection — this
 * logs a single concise WARN line instead: the task will simply
 * retry on its next scheduled run, and a repeated full stack trace
 * every 5 minutes during an outage adds cost without adding
 * diagnostic value after the first occurrence.
 *
 * <p>Everything else still gets the full ERROR log with stack
 * trace — this must never hide a genuine bug in scheduled logic,
 * only reduce noise for the specific class of failure that a human
 * can't act on differently the tenth time they see it versus the
 * first.
 */
@Component
public class TransientAwareErrorHandler implements ErrorHandler {

    /** Logger instance for logging task errors. */
    private static final Logger LOG =
            LoggerFactory.getLogger(TransientAwareErrorHandler.class);

    /**
     * SQLSTATE class {@code 08} is "Connection Exception" per the
     * SQL standard; PostgreSQL and most JDBC drivers follow this
     * convention for connection-level failures.
     */
    private static final String CONNECTION_EXCEPTION_SQLSTATE_CLASS = "08";

    @Override
    public final void handleError(final Throwable throwable) {
        if (isTransientInfrastructureFailure(throwable)) {
            LOG.warn("Scheduled task failed with a transient "
                            + "infrastructure error, will retry on the "
                            + "next run: {} - {}",
                    throwable.getClass().getSimpleName(),
                    throwable.getMessage());
            return;
        }

        LOG.error("Unexpected error occurred in scheduled task", throwable);
    }

    /**
     * Walks the full cause chain looking for a known transient,
     * infrastructure-level failure — not just the top-level wrapper
     * exception, since Spring, HikariCP, and the JDBC driver each
     * add their own wrapper on the way up (as seen with
     * CannotCreateTransactionException wrapping a lower-level
     * connection failure).
     *
     * @param throwable the exception thrown by a scheduled task
     * @return true if this is a transient connectivity issue that
     *         doesn't warrant a full stack trace on every occurrence
     */
    public boolean isTransientInfrastructureFailure(
            final Throwable throwable
    ) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CannotCreateTransactionException
                    || current instanceof TransientDataAccessException
                    || current instanceof SQLTransientException
                    || isConnectionSqlState(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isConnectionSqlState(final Throwable throwable) {
        if (!(throwable instanceof SQLException)) {
            return false;
        }

        String sqlState = ((SQLException) throwable).getSQLState();
        return sqlState != null
                && sqlState.startsWith(CONNECTION_EXCEPTION_SQLSTATE_CLASS);
    }
}
