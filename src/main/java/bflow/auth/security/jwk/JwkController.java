package bflow.auth.security.jwk;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

/**
 * Controller that exposes the JSON Web Key Set (JWKS) endpoint.
 */
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
public final class JwkController {

    /** Service for retrieving formatted JWK data. */
    private final JwkService jwkService;

    /**
     * Exposes the public keys used for JWT signature verification.
     * @return a map representing the JWKS (JSON Web Key Set).
     */
    @GetMapping("/jwks.json")
    public Map<String, Object> keys() {
        return jwkService.getJwks();
    }
}
