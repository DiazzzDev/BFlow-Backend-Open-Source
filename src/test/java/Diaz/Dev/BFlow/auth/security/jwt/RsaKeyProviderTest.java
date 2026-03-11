package Diaz.Dev.BFlow.auth.security.jwt;

import bflow.auth.security.jwt.RsaKeyProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RsaKeyProviderTest {

    @Test
    void rotateAddsNewKeyAndChangesActive() {
        RsaKeyProvider provider = new RsaKeyProvider();

        var before = provider.getAll();
        var activeBefore = provider.getActive();

        provider.rotate();

        var after = provider.getAll();
        var activeAfter = provider.getActive();

        assertTrue(after.size() >= before.size());
        assertNotEquals(activeBefore.kid(), activeAfter.kid());
        assertTrue(after.containsKey(activeAfter.kid()));
    }
}
