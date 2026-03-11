package Diaz.Dev.BFlow.auth.security.jwt;

import bflow.auth.security.jwt.RSAKeyLoader;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import java.io.InputStream;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

class RSAKeyLoaderTest {

    @Test
    void loadsPrivateKeyFromStream() throws Exception {
        RSAKeyLoader loader = new RSAKeyLoader();

        InputStream is = new ClassPathResource(
                "test-keys/private-test.pem"
        ).getInputStream();

        RSAPrivateKey key = loader.loadPrivateKey(is);

        assertNotNull(key);
        assertEquals("RSA", key.getAlgorithm());
    }

    @Test
    void loadsPublicKeyFromStream() throws Exception {
        RSAKeyLoader loader = new RSAKeyLoader();

        InputStream is = new ClassPathResource(
                "test-keys/public-test.pem"
        ).getInputStream();

        RSAPublicKey key = loader.loadPublicKey(is);

        assertNotNull(key);
        assertEquals("RSA", key.getAlgorithm());
    }
}
