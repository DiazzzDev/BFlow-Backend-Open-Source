package bflow.auth.security.jwt;

import org.springframework.stereotype.Service;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Service providing and rotating RSA key pairs for JWT signing.
 */
@Service
public final class RsaKeyProvider {

    /** RSA Key size constant. */
    private static final int RSA_KEY_SIZE = 2048;

    /** Map of key identifiers to key pairs. */
    private final Map<String, RsaKeyPair> keys = new HashMap<>();

    /** The currently active key identifier. */
    private String activeKid;

    /**
     * Initializes the provider with a default key.
     */
    public RsaKeyProvider() {
        RsaKeyPair key = generate("key-2026-01");
        keys.put(key.kid(), key);
        activeKid = key.kid();
    }

    /**
     * Retrieves the currently active key pair.
     * @return the active RsaKeyPair.
     */
    public RsaKeyPair getActive() {
        return keys.get(activeKid);
    }

    /**
     * Retrieves a public key by its identifier.
     * @param kid the key ID.
     * @return the RSAPublicKey.
     */
    public RSAPublicKey getPublicKey(final String kid) {
        RsaKeyPair pair = keys.get(kid);
        if (pair == null) {
            throw new SecurityException("Unknown kid");
        }
        return pair.publicKey();
    }

    /**
     * Rotates keys by generating a new pair and setting it as active.
     */
    public void rotate() {
        RsaKeyPair newKey = generate("key-" + System.currentTimeMillis());
        keys.put(newKey.kid(), newKey);
        activeKid = newKey.kid();
    }

    /**
     * Generates a new RSA key pair.
     * @param kid the identifier to assign.
     * @return a new RsaKeyPair.
     */
    private RsaKeyPair generate(final String kid) {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
            gen.initialize(RSA_KEY_SIZE);
            KeyPair kp = gen.generateKeyPair();

            return new RsaKeyPair(
                    kid,
                    kp.getPrivate(),
                    (RSAPublicKey) kp.getPublic()
            );
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a map of all public keys currently stored.
     * @return map of kid to RSAPublicKey.
     */
    public Map<String, RSAPublicKey> getAllPublicKeys() {
        Map<String, RSAPublicKey> publicKeys = new HashMap<>();

        for (Map.Entry<String, RsaKeyPair> entry : keys.entrySet()) {
            publicKeys.put(entry.getKey(), entry.getValue().publicKey());
        }

        return publicKeys;
    }

    /**
     * Returns a copy of all internal key pairs.
     * @return map of kid to RsaKeyPair.
     */
    public Map<String, RsaKeyPair> getAll() {
        return Map.copyOf(keys);
    }
}
