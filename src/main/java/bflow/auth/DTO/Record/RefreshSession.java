package bflow.auth.DTO.Record;

import java.time.Instant;
import java.util.UUID;

/**
 * Details of an active refresh session.
 * @param id session identifier.
 * @param createdAt creation timestamp.
 * @param expiresAt expiration timestamp.
 * @param current if this is the active session.
 */
public record RefreshSession(
        UUID id,
        Instant createdAt,
        Instant expiresAt,
        boolean current
) { }
