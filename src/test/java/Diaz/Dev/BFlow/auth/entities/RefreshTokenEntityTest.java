package Diaz.Dev.BFlow.auth.entities;

import bflow.auth.entities.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RefreshTokenEntityTest {

    @Test
    void testRefreshTokenCreation() {
        RefreshToken token = new RefreshToken();
        token.setId(UUID.randomUUID());
        token.setUserId(UUID.randomUUID());
        token.setTokenHash("hash123");
        token.setCreatedAt(Instant.now());
        token.setExpiresAt(Instant.now().plusSeconds(86400));
        token.setRevoked(false);

        assertNotNull(token.getId());
        assertNotNull(token.getUserId());
        assertEquals("hash123", token.getTokenHash());
        assertFalse(token.isRevoked());
    }

    @Test
    void testRefreshTokenRevocation() {
        RefreshToken token = new RefreshToken();
        token.setRevoked(false);
        assertFalse(token.isRevoked());

        token.setRevoked(true);
        assertTrue(token.isRevoked());
    }

    @Test
    void testRefreshTokenReplacedBy() {
        RefreshToken token = new RefreshToken();
        UUID replacement = UUID.randomUUID();
        token.setReplacedBy(replacement);

        assertEquals(replacement, token.getReplacedBy());
    }
}
