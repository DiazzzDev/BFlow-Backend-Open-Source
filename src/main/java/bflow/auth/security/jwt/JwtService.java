package bflow.auth.security.jwt;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for handling JWT operations.
 */
public interface JwtService {
    /**
     * Generates a signed JWT for a user.
     * @param userId user identifier.
     * @param email user email.
     * @param roles user roles.
     * @return serialized JWT string.
     */
    String generateToken(
            UUID userId,
            String email,
            List<String> roles
    );

    /**
     * Gets the TTL for access tokens.
     * @return duration in seconds.
     */
    long getAccessTokenTtlSeconds();

    /**
     * Validates if a token is authentic and not expired.
     * @param token serialized JWT.
     * @return true if valid.
     */
    boolean validateToken(String token);

    /**
     * Extracts user ID from token.
     * @param token serialized JWT.
     * @return UUID of the user.
     */
    UUID extractUserId(String token);

    /**
     * Extracts email from token.
     * @param token serialized JWT.
     * @return email string.
     */
    String extractEmail(String token);

    /**
     * Extracts roles from token.
     * @param token serialized JWT.
     * @return list of roles.
     */
    List<String> extractRoles(String token);

    /**
     * Attaches JWT and Refresh tokens to HTTP cookies.
     * @param response servlet response.
     * @param accessToken JWT string.
     * @param refreshToken Refresh token string.
     */
    void attachAuthCookies(
            HttpServletResponse response,
            String accessToken,
            String refreshToken
    );
}
