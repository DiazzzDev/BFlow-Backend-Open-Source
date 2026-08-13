package bflow.auth.services;

import bflow.auth.DTO.Record.SyncUserRequest;
import bflow.auth.DTO.Record.SyncUserResponse;
import bflow.auth.entities.User;
import bflow.auth.enums.PictureSource;
import bflow.auth.enums.UserStatus;
import bflow.auth.repository.RepositoryUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import bflow.auth.security.CognitoIdTokenValidator;

/**
 * Synchronizes Cognito users with the local database.
 */
@Service
@RequiredArgsConstructor
public class AuthSyncService {

    /**
     * Repository for user persistence.
     */
    private final RepositoryUser repositoryUser;

    /**
     * Service responsible for initial user setup.
     */
    private final AuthBootstrapService authBootstrapService;

    /**
     * Validator for Cognito ID tokens.
     */
    private final CognitoIdTokenValidator idTokenValidator;

    /**
     * Synchronizes the authenticated Cognito user with the local database.
     *
     * @param accessJwt validated access token
     * @param request synchronization request
     * @return synchronization result
     */
    @Transactional
    public SyncUserResponse synchronize(
            final Jwt accessJwt,
            final SyncUserRequest request
    ) {
        // Validate idToken signature with Cognito JWKs — no manual parsing
        Jwt idToken = idTokenValidator.validate(request.idToken());

        String sub = idToken.getSubject();
        String email = idToken.getClaimAsString("email");
        Boolean emailVerified = idToken.getClaimAsBoolean("email_verified");
        String name = idToken.getClaimAsString("name");
        String picture = idToken.getClaimAsString("picture");

        // Verify sub consistency between access token and id token
        if (!accessJwt.getSubject().equals(sub)) {
            throw new IllegalArgumentException(
                    "Token subject mismatch"
            );
        }

        Optional<User> existingBySub = repositoryUser.findByCognitoSub(sub);
        if (existingBySub.isPresent()) {
            User user = existingBySub.get();
            applyProfileClaims(user, name, picture);
            repositoryUser.save(user);
            return new SyncUserResponse(
                    user.getId(),
                    user.getEmail(),
                    String.join(",", user.getRoles()),
                    false
            );
        }

        Optional<User> existingByEmail = repositoryUser.findByEmail(email);
        if (existingByEmail.isPresent()) {
            User user = existingByEmail.get();
            user.setCognitoSub(sub);
            if (Boolean.TRUE.equals(emailVerified)) {
                user.setEmailVerified(true);
            }
            applyProfileClaims(user, name, picture);
            repositoryUser.save(user);
            return new SyncUserResponse(
                    user.getId(),
                    user.getEmail(),
                    String.join(",", user.getRoles()),
                    false
            );
        }

        User newUser = User.builder()
                .cognitoSub(sub)
                .email(email)
                .name(name)
                .pictureUrl(picture)
                .pictureSource(picture != null && !picture.isBlank()
                        ? PictureSource.GOOGLE : PictureSource.NONE)
                .status(UserStatus.ACTIVE)
                .emailVerified(Boolean.TRUE.equals(emailVerified))
                .roles(Set.of("ROLE_USER"))
                .build();

        repositoryUser.save(newUser);
        authBootstrapService.bootstrap(newUser);

        return new SyncUserResponse(
                newUser.getId(),
                newUser.getEmail(),
                "ROLE_USER",
                true
        );
    }

    /**
     * Updates the user's name and, if the picture didn't come from an S3
     * upload, their picture URL — sourced from the Cognito ID token.
     *
     * @param user the user to update
     * @param name the display name claim, or {@code null}
     * @param picture the picture URL claim, or {@code null}
     */
    private void applyProfileClaims(
            final User user,
            final String name,
            final String picture
    ) {
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }

        boolean canOverwritePicture =
                user.getPictureSource() != PictureSource.S3;

        if (picture != null && !picture.isBlank() && canOverwritePicture) {
            user.setPictureUrl(picture);
            user.setPictureSource(PictureSource.GOOGLE);
        }
    }
}
