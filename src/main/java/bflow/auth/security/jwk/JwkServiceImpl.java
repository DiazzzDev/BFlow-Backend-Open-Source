package bflow.auth.security.jwk;

import bflow.auth.security.jwt.RsaKeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link JwkService} that transforms RSA keys into JWK maps.
 */
@Service
@RequiredArgsConstructor
public final class JwkServiceImpl implements JwkService {

    /** Provider for retrieving the current RSA key pairs. */
    private final RsaKeyProvider rsaKeyProvider;

    @Override
    public Map<String, Object> getJwks() {

        List<Map<String, Object>> keys = rsaKeyProvider
                .getAll()
                .values()
                .stream()
                .map(k -> {
                    RSAPublicKey pub = k.publicKey();

                    Map<String, Object> jwk = new HashMap<>();
                    jwk.put("kty", "RSA");
                    jwk.put("kid", k.kid());
                    jwk.put("alg", "RS256");
                    jwk.put("use", "sig");
                    jwk.put("n", Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(pub.getModulus().toByteArray()));
                    jwk.put("e", Base64.getUrlEncoder()
                            .withoutPadding()
                            .encodeToString(pub.getPublicExponent()
                                    .toByteArray())
                    );

                    return jwk;
                })
                .toList();

        return Map.of("keys", keys);
    }
}
