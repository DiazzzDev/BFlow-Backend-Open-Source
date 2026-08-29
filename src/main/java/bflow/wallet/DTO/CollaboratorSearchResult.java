package bflow.wallet.DTO;

import bflow.wallet.enums.CollaboratorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

/**
 * A user match returned by the collaborator search, mirroring the
 * "Add people" GitHub-style picker: id, display name, email, avatar
 * and whether they can still be invited to the wallet.
 */
@Getter
@AllArgsConstructor
public class CollaboratorSearchResult {

    /** Unique identifier of the matched user. */
    private UUID id;

    /** Display name of the matched user. */
    private String name;

    /** Email address of the matched user. */
    private String email;

    /** URL of the user's profile picture, if any. */
    private String pictureUrl;

    /** Whether this user can still be invited to the wallet. */
    private CollaboratorStatus status;
}
