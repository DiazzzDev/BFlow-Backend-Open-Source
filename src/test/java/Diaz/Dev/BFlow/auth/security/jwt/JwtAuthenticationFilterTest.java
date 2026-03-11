package Diaz.Dev.BFlow.auth.security.jwt;

import bflow.auth.security.jwt.JwtAuthenticationFilter;
import bflow.auth.security.jwt.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class JwtAuthenticationFilterTest {
    private JwtService jwtService;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setup() {
        jwtService = mock(JwtService.class);
        filter = new JwtAuthenticationFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    void should_not_authenticate_when_no_cookie_present() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void should_not_authenticate_when_token_is_invalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "bad-token"));

        when(jwtService.validateToken("bad-token")).thenReturn(false);

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(request, response);
    }

    @Test
    void should_authenticate_when_token_is_valid() throws Exception {
        UUID userId = UUID.randomUUID();

        when(jwtService.validateToken("good-token")).thenReturn(true);
        when(jwtService.extractUserId("good-token")).thenReturn(userId);
        when(jwtService.extractEmail("good-token")).thenReturn("test@bflow.dev");
        when(jwtService.extractRoles("good-token")).thenReturn(List.of("USER", "ADMIN"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "good-token"));

        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();

        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(userId.toString());
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactlyInAnyOrder("USER", "ADMIN");

        assertThat(auth.getDetails())
                .isInstanceOfAny(java.util.Map.class);

        verify(chain).doFilter(request, response);
    }

}
