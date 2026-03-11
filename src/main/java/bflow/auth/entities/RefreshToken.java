package bflow.auth.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing an OAuth2/JWT refresh token.
 */
@Entity
@Table(name = "auth_refresh_tokens")
@Getter
@Setter
public class RefreshToken {

    /** Unique identifier for the token. */
    @Id
    private UUID id;

    /** The ID of the user this token belongs to. */
    @Column(nullable = false)
    private UUID userId;

    /** The hashed version of the refresh token. */
    @Column(nullable = false)
    private String tokenHash;

    /** The expiration timestamp of the token. */
    @Column(nullable = false)
    private Instant expiresAt;

    /** Whether the token has been revoked. */
    @Column(nullable = false)
    private boolean revoked = false;

    /** The timestamp when the token was issued. */
    @Column(nullable = false)
    private Instant createdAt;

    /** The ID of the new token that replaced this one during rotation. */
    private UUID replacedBy;
}

