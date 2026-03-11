package bflow.auth.DTO.Record;

/**
 * Simple token response.
 * @param token the generated JWT.
 */
public record AuthResponse(
        String token
) { }
