package bflow.auth.DTO.Record;

import java.util.UUID;

/**
 * Result of a refresh token rotation.
 * @param userId the owner of the token.
 * @param newRefreshToken the newly generated refresh token.
 */
public record RefreshRotationResult(
        UUID userId,
        String newRefreshToken
) { }
