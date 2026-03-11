package bflow.auth.security.jwk;

import java.util.Map;

/**
 * Service interface for managing and exposing JSON Web Keys (JWK).
 */
public interface JwkService {
    /**
     * Retrieves the collection of public keys in JWKS format.
     * @return a map containing the list of JWKs.
     */
    Map<String, Object> getJwks();
}
