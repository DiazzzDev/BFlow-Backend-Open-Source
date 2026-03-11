package bflow.auth.repository;

import bflow.auth.entities.AuthAccount;
import bflow.auth.enums.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing {@link AuthAccount} entities.
 */
@Repository
public interface RepositoryAuthAccount
        extends JpaRepository<AuthAccount, UUID> {

    /**
     * Finds an active account by email and provider.
     * @param login the user email.
     * @param provider the auth provider.
     * @return an optional containing the account if found.
     */
    @Query("""
        SELECT a
        FROM AuthAccount a
        JOIN FETCH a.user u
        WHERE u.email = :login
          AND a.provider = :provider
          AND a.enabled = true
    """)
    Optional<AuthAccount> findActiveByLoginAndProvider(
            @Param("login") String login,
            @Param("provider") AuthProvider provider
    );

    /**
     * Checks if an account exists for a specific provider user ID.
     * @param provider the auth provider.
     * @param providerUserId the external provider's ID.
     * @return true if it exists.
     */
    boolean existsByProviderAndProviderUserId(
            AuthProvider provider,
            String providerUserId
    );
}
