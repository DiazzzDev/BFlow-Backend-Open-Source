package bflow.auth.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO containing the current authenticated user's profile information.
 */
@Getter
@Setter
public class AuthMeResponse {

    /** The unique identifier of the user. */
    private UUID userId;

    /** The user's primary email address. */
    private String email;

    /** The list of security roles assigned to the user. */
    private List<String> roles;
}
