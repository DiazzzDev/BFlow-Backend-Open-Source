package Diaz.Dev.BFlow.auth.DTO;

import bflow.auth.DTO.AuthLoginRequest;
import bflow.auth.DTO.AuthRegisterRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthDTOTest {

    @Test
    void testAuthLoginRequest() {
        AuthLoginRequest request = new AuthLoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("password123");

        assertEquals("user@test.com", request.getEmail());
        assertEquals("password123", request.getPassword());
    }

    @Test
    void testAuthRegisterRequest() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setEmail("new@test.com");
        request.setPassword("secure123");

        assertEquals("new@test.com", request.getEmail());
        assertEquals("secure123", request.getPassword());
    }
}
