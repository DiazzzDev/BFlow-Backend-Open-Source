package bflow.wallet.repository.spec;

import bflow.wallet.entities.WalletUser;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Specifications reutilizables para consultas dinámicas sobre WalletUser.
 * Se combinan en el service con Specification.where(...).and(...)
 * para evitar explotar el repositorio con un método por combinación de filtros.
 */
public final class WalletSpecs {

    private WalletSpecs() { }

    /** Restringe resultados a las wallets donde participa el usuario autenticado. */
    public static Specification<WalletUser> byUser(final UUID userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    /** Búsqueda case-insensitive por nombre de wallet. */
    public static Specification<WalletUser> nameContains(final String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.conjunction();
            }
            var wallet = root.join("wallet"); // Wallet
            String uq = "%" + q.trim().toUpperCase() + "%";
            return cb.like(cb.upper(wallet.get("name")), uq);
        };
    }
}