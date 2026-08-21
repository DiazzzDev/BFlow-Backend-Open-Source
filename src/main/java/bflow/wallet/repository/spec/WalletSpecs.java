package bflow.wallet.repository.spec;

import bflow.wallet.entities.WalletUser;
import bflow.wallet.enums.WalletRole;
import bflow.wallet.enums.WalletScope;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Utility class containing wallet query specifications.
 */
public final class WalletSpecs {

    /**
     * Utility class.
     */
    private WalletSpecs() { }

    /**
     * Creates a specification that filters wallet memberships by user.
     *
     * @param userId user identifier.
     * @return specification matching the given user.
     */
    public static Specification<WalletUser> byUser(final UUID userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    /**
     * Creates a specification that filters wallets whose name contains the
     * provided search text.
     *
     * @param q optional search text.
     * @return specification matching wallet names.
     */
    public static Specification<WalletUser> nameContains(final String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            var wallet = root.join("wallet");
            String uq = "%" + q.trim().toUpperCase() + "%";
            return cb.like(cb.upper(wallet.get("name")), uq);
        };
    }

    /**
     * Creates a specification that filters wallet memberships by role.
     *
     * @param role wallet role filter.
     * @return specification matching the requested role.
     */
    public static Specification<WalletUser> byRole(final WalletRole role) {
        return (root, query, cb) -> {
            if (role == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("role"), role);
        };
    }

    /**
     * Creates a specification that filters wallets according to their scope.
     *
     * MINE returns wallets where the authenticated user is the only member.
     * SHARED returns wallets with more than one member.
     *
     * @param scope wallet scope filter.
     * @return specification matching the requested wallet scope.
     */
    public static Specification<WalletUser> byScope(final WalletScope scope) {
        return (root, query, cb) -> {
            if (scope == null) {
                return cb.conjunction();
            }

            // Subquery: cuenta TODOS los miembros de la misma wallet
            // (root.wallet.id), sin filtrar por usuario.
            Subquery<Long> memberCount = query.subquery(Long.class);
            var wu = memberCount.from(WalletUser.class);
            memberCount.select(cb.count(wu));
            memberCount.where(cb.equal(
                    wu.get("wallet").get("id"),
                    root.get("wallet").get("id")
            ));

            return switch (scope) {
                case MINE -> cb.equal(memberCount, 1L);
                case SHARED -> cb.greaterThan(memberCount, 1L);
            };
        };
    }
}
