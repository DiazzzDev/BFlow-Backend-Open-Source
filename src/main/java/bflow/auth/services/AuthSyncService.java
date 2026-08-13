package bflow.auth.services;

import bflow.auth.DTO.Record.SyncUserRequest;
import bflow.auth.DTO.Record.SyncUserResponse;
import bflow.auth.DTO.UserMeResponse;
import bflow.auth.entities.User;
import bflow.auth.enums.PictureSource;
import bflow.auth.enums.UserStatus;
import bflow.auth.mapper.UserMapper;
import bflow.auth.repository.RepositoryUser;
import bflow.auth.security.CognitoIdTokenValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

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
     * Mapper for building user-facing response DTOs.
     */
    private final UserMapper userMapper;

    /**
     * Synchronizes the authenticated Cognito user with the local database
     * and returns the initial session data required by the client.
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
        Jwt idToken = idTokenValidator.validate(request.idToken());

        String sub = idToken.getSubject();
        String email = idToken.getClaimAsString("email");
        Boolean emailVerified = idToken.getClaimAsBoolean("email_verified");
        String name = idToken.getClaimAsString("name");
        String picture = idToken.getClaimAsString("picture");

        if (!accessJwt.getSubject().equals(sub)) {
            throw new IllegalArgumentException("Token subject mismatch");
        }

        Optional<User> existingBySub = repositoryUser.findByCognitoSub(sub);

        if (existingBySub.isPresent()) {
            User user = existingBySub.get();

            applyProfileClaims(user, name, picture);
            repositoryUser.save(user);

            return buildResponse(user, false);
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

            return buildResponse(user, false);
        }

        User newUser = User.builder()
                .cognitoSub(sub)
                .email(email)
                .name(name)
                .pictureUrl(picture)
                .pictureSource(
                        picture != null && !picture.isBlank()
                                ? PictureSource.GOOGLE
                                : PictureSource.NONE
                )
                .status(UserStatus.ACTIVE)
                .emailVerified(Boolean.TRUE.equals(emailVerified))
                .roles(Set.of("ROLE_USER"))
                .build();

        repositoryUser.save(newUser);
        authBootstrapService.bootstrap(newUser);

        return buildResponse(newUser, true);
    }

    /**
     * Builds the session response returned after authentication
     * synchronization.
     *
     * @param user authenticated user
     * @param isNewUser whether the user was created during synchronization
     * @return complete synchronization response
     */
    private SyncUserResponse buildResponse(
            final User user,
            final boolean isNewUser
    ) {
        UserMeResponse meResponse = userMapper.toMeResponse(user);

        return new SyncUserResponse(
                meResponse.id(),
                user.getEmail(),
                meResponse.roles(),
                isNewUser,
                meResponse.subscription(),
                meResponse.wallets(),
                meResponse.profile()
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
