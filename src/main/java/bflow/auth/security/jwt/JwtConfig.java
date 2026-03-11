package bflow.auth.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Configuration for JWT decoding using JWKS.
 */
@Configuration
public class JwtConfig {

    /**
     * Configures the JwtDecoder bean.
     * @return the NimbusJwtDecoder instance.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
                .withJwkSetUri("http://localhost:8080/.well-known/jwks.json")
                .build();
    }
}

