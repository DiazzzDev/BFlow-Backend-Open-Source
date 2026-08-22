package Diaz.Dev.BFlow.common.scheduling;

import bflow.common.scheduling.TransientAwareErrorHandler;

import org.junit.jupiter.api.Test;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.transaction.CannotCreateTransactionException;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for TransientAwareErrorHandler. The classification
 * logic is what matters here — misclassifying a genuine bug as
 * "transient" would silently downgrade it to a WARN and hide it
 * from anyone watching ERROR-level alerts, so the false-negative
 * cases (real bugs) get just as much coverage as the true-positive
 * cases (actual transient infrastructure failures).
 */
class TransientAwareErrorHandlerTest {

    private final TransientAwareErrorHandler handler =
            new TransientAwareErrorHandler();

    // ---- True positives: transient infrastructure failures ----

    @Test
    void isTransient_cannotCreateTransactionException_true() {
        CannotCreateTransactionException ex =
                new CannotCreateTransactionException(
                        "Could not open JPA EntityManager for transaction");

        assertTrue(handler.isTransientInfrastructureFailure(ex));
    }

    @Test
    void isTransient_wrappedTwoLevelsDeep_stillDetected() {
        // Exactly the shape Spring produces in practice: Hibernate's
        // exception wraps HikariCP's, which wraps the raw SQLException.
        SQLTransientConnectionException root =
                new SQLTransientConnectionException("Connection is not available");
        CannotCreateTransactionException wrapped =
                new CannotCreateTransactionException(
                        "Could not open JPA EntityManager", root);

        assertTrue(handler.isTransientInfrastructureFailure(wrapped));
    }

    @Test
    void isTransient_sqlExceptionWithConnectionSqlState_true() {
        // SQLSTATE 08006 = "connection failure" per the SQL standard.
        SQLException ex = new SQLException(
                "Connection reset by peer", "08006");

        assertTrue(handler.isTransientInfrastructureFailure(ex));
    }

    @Test
    void isTransient_sqlTransientException_true() {
        SQLTransientConnectionException ex =
                new SQLTransientConnectionException("Pool exhausted");

        assertTrue(handler.isTransientInfrastructureFailure(ex));
    }

    @Test
    void isTransient_springTransientDataAccessException_true() {
        QueryTimeoutException ex =
                new QueryTimeoutException("Query timed out");

        assertTrue(handler.isTransientInfrastructureFailure(ex));
    }

    // ---- True negatives: genuine bugs must NOT be downgraded ----

    @Test
    void isTransient_nullPointerException_false() {
        assertFalse(handler.isTransientInfrastructureFailure(
                new NullPointerException("category was null")));
    }

    @Test
    void isTransient_sqlExceptionWithNonConnectionSqlState_false() {
        // 23505 = unique_violation — a real data/logic problem, not
        // an infrastructure blip. Must stay at full ERROR visibility.
        SQLException ex = new SQLException(
                "duplicate key value violates unique constraint",
                "23505");

        assertFalse(handler.isTransientInfrastructureFailure(ex));
    }

    @Test
    void isTransient_sqlExceptionWithNullSqlState_false() {
        SQLException ex = new SQLException("Unknown error");

        assertFalse(handler.isTransientInfrastructureFailure(ex));
    }

    @Test
    void isTransient_illegalStateException_false() {
        assertFalse(handler.isTransientInfrastructureFailure(
                new IllegalStateException(
                        "Recurring transaction not found")));
    }

    @Test
    void isTransient_causeChainWithNoMatchAnywhere_terminatesAsFalse() {
        RuntimeException deeplyNested = new RuntimeException(
                "outer", new RuntimeException("middle",
                new IllegalArgumentException("root cause")));

        assertFalse(handler.isTransientInfrastructureFailure(
                deeplyNested));
    }

    // ---- handleError must never throw, regardless of classification ----
    // A scheduled task's error handler throwing would be worse than
    // the original failure — it could break the scheduler thread.

    @Test
    void handleError_transientFailure_doesNotThrow() {
        assertDoesNotThrow(() -> handler.handleError(
                new CannotCreateTransactionException("DB unavailable")));
    }

    @Test
    void handleError_unexpectedFailure_doesNotThrow() {
        assertDoesNotThrow(() -> handler.handleError(
                new NullPointerException("unexpected null")));
    }

    @Test
    void handleError_exceptionWithNullMessage_doesNotThrow() {
        assertDoesNotThrow(() -> handler.handleError(
                new CannotCreateTransactionException(null)));
    }
}
