package bflow.auth.security.jwt;

import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Record holding an RSA key pair and its identifier.
 * @param kid the key identifier.
 * @param privateKey the RSA private key.
 * @param publicKey the RSA public key.
 */
public record RsaKeyPair(
        String kid,
        PrivateKey privateKey,
        RSAPublicKey publicKey
) { }
