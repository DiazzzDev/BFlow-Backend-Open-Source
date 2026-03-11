package bflow.auth.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration for password hashing using Argon2.
 */
@Configuration
public class PasswordEncoderConfig {
    /** The salt length in bytes. */
    private static final int SALT_LENGTH = 16;
    /** The hash length in bytes. */
    private static final int HASH_LENGTH = 32;
    /** The degree of parallelism. */
    private static final int PARALLELISM = 8;
    /** The memory cost in KB (64MB). */
    private static final int MEMORY = 65536;
    /** The number of iterations. */
    private static final int ITERATIONS = 10;

    /**
     * Provides the Argon2 password encoder bean.
     * @return an Argon2PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Argon2PasswordEncoder(
                SALT_LENGTH,
                HASH_LENGTH,
                PARALLELISM,
                MEMORY,
                ITERATIONS
        );
    }
}
