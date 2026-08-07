package bflow.budget.repository.spec;

import bflow.budget.DTO.BudgetSearchRequest;
import bflow.budget.entity.Budget;
import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.BudgetStatus;
import bflow.budget.enums.PeriodType;
import bflow.category.entity.Category;
import bflow.wallet.entities.Wallet;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds dynamic {@link Specification} predicates for querying
 * {@link Budget} entities.
 *
 * <p>Each static method returns a small, independent, composable
 * predicate. New filters can be added here as additional static
 * methods without touching the repository or existing callers,
 * keeping {@code RepositoryBudget} free of one derived-query method
 * per filter combination.
 */
public final class BudgetSpecification {

    /**
     * Private constructor to prevent instantiation.
     */
    private BudgetSpecification() {
    }

    /**
     * Restricts results to budgets owned by the given user.
     * This predicate is always applied to enforce per-user data
     * isolation, regardless of which other filters are present.
     *
     * @param userId the authenticated user's ID
     * @return a specification filtering by owner
     */
    public static Specification<Budget> belongsToUser(final UUID userId) {
        return (root, query, cb) ->
                cb.equal(root.get("user").get("id"), userId);
    }

    /**
     * Case-insensitive partial match against the associated wallet's
     * name or the associated category's name.
     *
     * @param name the search term (may be null or blank)
     * @return a specification filtering by name, or null when the
     *         term is not provided
     */
    public static Specification<Budget> nameContains(final String name) {

        if (name == null || name.isBlank()) {
            return null;
        }

        final String pattern = "%" + name.trim().toLowerCase() + "%";

        return (root, query, cb) -> {

            // Avoid duplicated rows caused by the joins below.
            if (query != null) {
                query.distinct(true);
            }

            Join<Budget, Wallet> walletJoin =
                    root.join("wallet", JoinType.LEFT);
            Join<Budget, Category> categoryJoin =
                    root.join("category", JoinType.LEFT);

            Predicate walletMatch = cb.like(
                    cb.lower(walletJoin.get("name")), pattern
            );

            Predicate categoryMatch = cb.like(
                    cb.lower(categoryJoin.get("name")), pattern
            );

            return cb.or(walletMatch, categoryMatch);
        };
    }

    /**
     * Filters by wallet ID.
     *
     * @param walletId the wallet ID
     * @return a specification filtering by wallet, or null if absent
     */
    public static Specification<Budget> hasWallet(final UUID walletId) {

        if (walletId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(root.get("wallet").get("id"), walletId);
    }

    /**
     * Filters by category ID.
     *
     * @param categoryId the category ID
     * @return a specification filtering by category, or null if absent
     */
    public static Specification<Budget> hasCategory(final UUID categoryId) {

        if (categoryId == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filters by budget scope.
     *
     * @param scope the budget scope
     * @return a specification filtering by scope, or null if absent
     */
    public static Specification<Budget> hasScope(final BudgetScope scope) {

        if (scope == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("scope"), scope);
    }

    /**
     * Filters by period type.
     *
     * @param period the period type
     * @return a specification filtering by period, or null if absent
     */
    public static Specification<Budget> hasPeriod(final PeriodType period) {

        if (period == null) {
            return null;
        }

        return (root, query, cb) -> cb.equal(root.get("period"), period);
    }

    /**
     * Filters by the budget's last alert status.
     *
     * @param status the budget status
     * @return a specification filtering by status, or null if absent
     */
    public static Specification<Budget> hasStatus(final BudgetStatus status) {

        if (status == null) {
            return null;
        }

        return (root, query, cb) ->
                cb.equal(root.get("lastAlertStatus"), status);
    }

    /**
     * Combines every optional filter present in a
     * {@link BudgetSearchRequest} with the mandatory ownership
     * restriction into a single dynamic specification.
     *
     * @param filter the search filters (individual fields may be null)
     * @param userId the authenticated user's ID (always enforced)
     * @return the combined specification ready to be executed
     */
    public static Specification<Budget> build(
            final BudgetSearchRequest filter,
            final UUID userId
    ) {

        Specification<Budget> combined = belongsToUser(userId);

        List<Specification<Budget>> optionalSpecs = new ArrayList<>();
        optionalSpecs.add(nameContains(filter.getQuery()));
        optionalSpecs.add(hasWallet(filter.getWalletId()));
        optionalSpecs.add(hasCategory(filter.getCategoryId()));
        optionalSpecs.add(hasScope(filter.getScope()));
        optionalSpecs.add(hasPeriod(filter.getPeriod()));
        optionalSpecs.add(hasStatus(filter.getStatus()));

        for (Specification<Budget> spec : optionalSpecs) {
            if (spec != null) {
                combined = combined.and(spec);
            }
        }

        return combined;
    }
}
