package bflow.auth.repository.specs;

import bflow.auth.entities.User;
import bflow.auth.enums.UserStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

/**
 * Utility class containing user query specifications.
 */
public final class UserSpecs {

    /**
     * Utility class.
     */
    private UserSpecs() { }

    /**
     * Creates a specification that matches users whose name or email
     * contains the provided search text, case-insensitively.
     *
     * @param q search text.
     * @return specification matching the given text.
     */
    public static Specification<User> nameOrEmailContains(final String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) {
                return cb.disjunction();
            }
            String pattern = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern)
            );
        };
    }

    /**
     * Creates a specification that excludes a specific user by ID.
     *
     * @param userId user identifier to exclude.
     * @return specification excluding the given user.
     */
    public static Specification<User> excludeUser(final UUID userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return cb.conjunction();
            }
            return cb.notEqual(root.get("id"), userId);
        };
    }

    /**
     * Creates a specification that matches only active users.
     * Suspended/deleted accounts should never appear as invitable
     * collaborators.
     *
     * @return specification matching active users.
     */
    public static Specification<User> isActive() {
        return (root, query, cb) ->
                cb.equal(root.get("status"), UserStatus.ACTIVE);
    }
}
