package bflow.auth.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request DTO for user login credentials.
 */
@Getter
@Setter
public class AuthLoginRequest {

    /** Minimum length for an email address. */
    private static final int MIN_EMAIL = 5;
    /** Maximum length for email and password fields. */
    private static final int MAX_LENGTH = 255;

    /** The user's email address. */
    @Email
    @Size(min = MIN_EMAIL, max = MAX_LENGTH,
            message = "Email must be between 5 to 255 characters")
    private String email;

    /** The user's plain-text password. */
    @Size(max = MAX_LENGTH,
            message = "Password cannot be longer than 255 characters")
    private String password;
}
