package bflow.auth.security.jwk;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * Utility class for mapping RSA public keys to JSON Web Key (JWK) format.
 */
public final class JwkMapper {

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private JwkMapper() {
        // Prevent instantiation
    }

    /**
     * Converts an RSA Public Key into a JWK DTO.
     * @param key the RSA public key.
     * @param kid the key identifier.
     * @return a mapped Jwk object.
     */
    public static Jwk from(final RSAPublicKey key, final String kid) {
        return Jwk.builder()
                .kty("RSA")
                .kid(kid)
                .use("sig")
                .alg("RS256")
                .n(base64Url(key.getModulus().toByteArray()))
                .e(base64Url(key.getPublicExponent().toByteArray()))
                .build();
    }

    /**
     * Encodes a byte array into a Base64URL string without padding.
     * @param value the bytes to encode.
     * @return Base64URL encoded string.
     */
    private static String base64Url(final byte[] value) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value);
    }
}
