package bflow.auth.DTO.Record;

import jakarta.validation.constraints.Email;

/**
 * Request payload for authentication.
 * @param email user email.
 * @param password user password.
 */
public record AuthRequest(
        @Email
        String email,
        String password
) { }
