package Diaz.Dev.BFlow.auth.security.jwk;

import bflow.auth.security.jwk.JwkServiceImpl;
import bflow.auth.security.jwt.RsaKeyProvider;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JwkServiceTest {

    @Test
    void jwksContainsKeysAndAlgIsRS256() {
        RsaKeyProvider provider = new RsaKeyProvider();
        JwkServiceImpl service = new JwkServiceImpl(provider);

        Map<String, Object> jwks = service.getJwks();

        assertTrue(jwks.containsKey("keys"));
        var keys = (java.util.List<Map<String, Object>>) jwks.get("keys");
        assertFalse(keys.isEmpty());
        var first = keys.get(0);
        assertEquals("RS256", first.get("alg"));
        assertNotNull(first.get("kid"));
        assertNotNull(first.get("n"));
        assertNotNull(first.get("e"));
    }
}
