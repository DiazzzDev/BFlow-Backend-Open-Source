package bflow.auth.security.jwt;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.springframework.stereotype.Component;

/**
 * Utility class to load RSA keys from PEM formatted streams.
 */
@Component
public class RSAKeyLoader {

    /**
     * Loads a private key from an input stream.
     * @param is the input stream containing the PEM key.
     * @return the parsed RSAPrivateKey.
     * @throws Exception if parsing fails.
     */
    public RSAPrivateKey loadPrivateKey(final InputStream is) throws Exception {
        String key = new String(is.readAllBytes())
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA")
                .generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    /**
     * Loads a public key from an input stream.
     * @param is the input stream containing the PEM key.
     * @return the parsed RSAPublicKey.
     * @throws Exception if parsing fails.
     */
    public RSAPublicKey loadPublicKey(final InputStream is) throws Exception {
        String key = new String(is.readAllBytes())
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decoded = Base64.getDecoder().decode(key);
        return (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
    }
}
