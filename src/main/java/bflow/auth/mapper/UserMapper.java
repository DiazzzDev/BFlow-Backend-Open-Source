package bflow.auth.mapper;

import bflow.auth.DTO.UserMeResponse;
import bflow.auth.DTO.user.UserProfileResponse;
import bflow.auth.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps User entities to their public-facing response DTOs.
 */
@Component
public class UserMapper {

    /**
     * Builds the "/auth/me" response for the given user.
     *
     * <p>Subscription and wallets are intentionally left {@code null}/
     * empty here — they are populated by their respective services once
     * that wiring is in place, not by this mapper.
     *
     * @param user the authenticated user entity
     * @return the assembled response DTO
     */
    public UserMeResponse toMeResponse(final User user) {
        return new UserMeResponse(
                user.getId(),
                List.copyOf(user.getRoles()),
                null,
                List.of(),
                toProfileResponse(user)
        );
    }

    /**
     * Maps a User entity to its public profile representation.
     *
     * @param user the user entity
     * @return the mapped profile response
     */
    public UserProfileResponse toProfileResponse(final User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .pictureUrl(user.getPictureUrl())
                .roles(user.getRoles())
                .status(user.getStatus())
                .build();
    }
}
