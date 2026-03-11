package bflow.auth.repository;

import bflow.auth.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link RefreshToken} entities.
 */
@Repository
public interface RepositoryRefreshToken
        extends JpaRepository<RefreshToken, UUID> {

    /**
     * Finds a token by its hash.
     * @param tokenHash the hashed token.
     * @return optional containing the token.
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Finds all active tokens for a user.
     * @param userId the user ID.
     * @return list of active tokens.
     */
    List<RefreshToken> findAllByUserIdAndRevokedFalse(UUID userId);

    /**
     * Finds all valid tokens that have not expired.
     * @param userId the user ID.
     * @param now the current time to check against expiry.
     * @return list of valid tokens.
     */
    List<RefreshToken> findAllByUserIdAndRevokedFalseAndExpiresAtAfter(
            UUID userId,
            Instant now
    );

}
