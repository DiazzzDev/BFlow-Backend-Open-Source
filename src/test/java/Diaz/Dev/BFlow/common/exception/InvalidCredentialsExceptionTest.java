package Diaz.Dev.BFlow.common.exception;

import bflow.common.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidCredentialsExceptionTest {

    @Test
    void testExceptionIsThrowable() {
        InvalidCredentialsException exception = new InvalidCredentialsException();
        assertNotNull(exception);
        assertTrue(exception instanceof Exception);
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    void testExceptionCanBeThrown() {
        assertThrows(InvalidCredentialsException.class, () -> {
            throw new InvalidCredentialsException();
        });
    }
}
