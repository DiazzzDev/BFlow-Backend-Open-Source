package bflow.auth.DTO.Record;

/**
 * Request payload for authentication.
 * @param email user email.
 * @param password user password.
 */
public record AuthRequest(
        String email,
        String password
) { }
