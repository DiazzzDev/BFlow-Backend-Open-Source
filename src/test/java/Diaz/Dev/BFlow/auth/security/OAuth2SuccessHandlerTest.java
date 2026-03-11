package Diaz.Dev.BFlow.auth.security;

import bflow.auth.entities.User;
import bflow.auth.enums.AuthProvider;
import bflow.auth.security.jwt.JwtService;
import bflow.auth.services.UserService;
import bflow.auth.security.OAuth2SuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class OAuth2SuccessHandlerTest {

    @Mock
    JwtService jwtService;
    @Mock
    UserService userService;

    OAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new OAuth2SuccessHandler(jwtService, userService);
    }

    @Test
    void redirectsWithTokenInQuery() throws Exception {
        // prepare OAuth2User stub
        OAuth2User user = new OAuth2User() {
            @Override
            public Map<String, Object> getAttributes() {
                return Map.of("email", "a@b.com", "sub", "prov-1");
            }

            @Override
            public List getAuthorities() {
                return List.of();
            }

            @Override
            public String getName() {
                return "a@b.com";
            }
        };

        Authentication auth = new Authentication() {
            @Override public List getAuthorities() { return List.of(); }
            @Override public Object getCredentials() { return null; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return user; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {}
            @Override public String getName() { return "a@b.com"; }
        };

        User u = new User();
        u.setId(UUID.randomUUID());
        u.setEmail("a@b.com");
        u.setProvider(AuthProvider.GOOGLE);
        u.setRoles(java.util.Set.of("USER"));

        when(userService.resolveOAuth2User("a@b.com", "prov-1", AuthProvider.GOOGLE)).thenReturn(u);
        when(jwtService.generateToken(u.getId(), u.getEmail(), List.of("ROLE_USER"))).thenReturn("token-in-url");

        MockHttpServletResponse res = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(null, res, auth);

        assertEquals("http://localhost:3000", res.getRedirectedUrl());
    }
}
