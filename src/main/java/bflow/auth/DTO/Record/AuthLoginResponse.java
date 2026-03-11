package bflow.auth.DTO.Record;

/**
 * Data returned after a successful login.
 * @param accessToken the JWT access token.
 * @param tokenType the type of token.
 * @param expiresIn expiration time in seconds.
 */
public record AuthLoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) { }
