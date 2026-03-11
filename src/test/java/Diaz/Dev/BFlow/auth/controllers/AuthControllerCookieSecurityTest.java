package Diaz.Dev.BFlow.auth.controllers;

import bflow.auth.DTO.AuthLoginRequest;
import bflow.auth.entities.User;
import bflow.auth.security.jwt.JwtService;
import bflow.auth.services.AuthService;
import bflow.auth.services.ServiceRefreshToken;
import bflow.auth.controllers.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class AuthControllerCookieSecurityTest {

    @Mock
    AuthService authService;
    @Mock
    JwtService jwtService;
    @Mock
    ServiceRefreshToken serviceRefreshToken;

    @InjectMocks
    AuthController controller;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginSetsInsecureCookieFlags() {
        AuthLoginRequest req = new AuthLoginRequest();
        req.setEmail("a@b.com");
        req.setPassword("pass");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("a@b.com");

        when(authService.authenticate(req.getEmail(), req.getPassword())).thenReturn(user);
        when(authService.getRoles(user)).thenReturn(List.of("USER"));
        when(jwtService.generateToken(user.getId(), user.getEmail(), List.of("USER"))).thenReturn("token123");

        MockHttpServletResponse res = new MockHttpServletResponse();

        controller.login(req, res);

        var headers = res.getHeaders("Set-Cookie");
        assertTrue(headers.stream().anyMatch(h -> h.contains("access_token")));
        assertTrue(headers.stream().anyMatch(h -> h.contains("refresh_token")));

        // Current implementation: insecure flags
        assertTrue(headers.stream().anyMatch(h -> h.contains("SameSite=None")));
    }
}
