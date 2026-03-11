package Diaz.Dev.BFlow.auth.security.jwt;

import bflow.auth.security.jwt.RsaKeyPair;
import org.junit.jupiter.api.Test;

import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

class RsaKeyPairTest {

    @Test
    void testRsaKeyPairCanBeCreated() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();

        RsaKeyPair keyPair = new RsaKeyPair(
                "test-kid",
                pair.getPrivate(),
                (RSAPublicKey) pair.getPublic()
        );

        assertEquals("test-kid", keyPair.kid());
        assertNotNull(keyPair.privateKey());
        assertNotNull(keyPair.publicKey());
    }

    @Test
    void testKeyPairRecordIsImmutable() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();

        RsaKeyPair keyPair = new RsaKeyPair(
                "immutable-kid",
                pair.getPrivate(),
                (RSAPublicKey) pair.getPublic()
        );

        RsaKeyPair keyPair2 = new RsaKeyPair(
                "different-kid",
                pair.getPrivate(),
                (RSAPublicKey) pair.getPublic()
        );

        assertNotEquals(keyPair.kid(), keyPair2.kid());
    }
}
