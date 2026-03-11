package bflow.auth.security.jwk;

import lombok.Builder;

/**
 * Representation of a JSON Web Key (JWK) for public key exposure.
 * @param kty Key Type.
 * @param kid Key ID.
 * @param use Public Key Use.
 * @param alg Algorithm.
 * @param n RSA Modulus.
 * @param e RSA Exponent.
 */
@Builder
public record Jwk(
        String kty,
        String kid,
        String use,
        String alg,
        String n,
        String e
) { }
