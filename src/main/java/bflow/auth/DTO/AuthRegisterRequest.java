package bflow.auth.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object for user registration requests.
 */
@Getter
@Setter
public class AuthRegisterRequest {
    /** Minimum length for email. */
    private static final int MIN_EMAIL = 5;
    /** Maximum length for email. */
    private static final int MAX_EMAIL = 255;
    /** Maximum length for password. */
    private static final int MAX_PWD = 255;
    /** Minimum length for full name. */
    private static final int MIN_NAME = 3;
    /** Maximum length for full name. */
    private static final int MAX_NAME = 150;

    /** The user's email address. */
    @Email
    @NotBlank
    @Size(min = MIN_EMAIL, max = MAX_EMAIL,
            message = "Email must be between 5 to 255 characters")
    private String email;

    /** The user's password. */
    @NotBlank
    @Size(max = MAX_PWD,
            message = "Password cannot be longer than 255 characters")
    private String password;

    /** The user's full name. */
    @NotBlank
    @Size(min = MIN_NAME, max = MAX_NAME,
            message = "The name must be between 3 to 150 characters")
    private String fullName;
}
