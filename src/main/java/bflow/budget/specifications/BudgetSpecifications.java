package bflow.budget.specifications;

import bflow.budget.DTO.BudgetSearchCriteria;
import bflow.budget.entity.Budget;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

/**
 * Reusable, composable JPA specifications for budget searches.
 */
public final class BudgetSpecifications {

    private BudgetSpecifications() {
    }

    /**
     * Builds the complete search predicate. Ownership is always mandatory.
     *
     * @param criteria optional client-provided filters
     * @param userId authenticated user identifier
     * @return combined specification
     */
    public static Specification<Budget> from(
            final BudgetSearchCriteria criteria,
            final UUID userId
    ) {
        BudgetSearchCriteria effectiveCriteria = criteria == null
                ? new BudgetSearchCriteria() : criteria;
        Specification<Budget> specification = ownedBy(userId);

        specification = andIfPresent(
                specification, nameContains(effectiveCriteria.getName()));
        specification = andIfPresent(
                specification, walletIdEquals(effectiveCriteria.getWalletId()));
        specification = andIfPresent(
                specification, periodEquals(effectiveCriteria.getPeriod()));
        specification = andIfPresent(
                specification, scopeEquals(effectiveCriteria.getScope()));
        specification = andIfPresent(
                specification,
                startDateOnOrAfter(effectiveCriteria.getStartDateFrom()));
        return andIfPresent(
                specification,
                startDateOnOrBefore(effectiveCriteria.getStartDateTo()));
    }

    /**
     * Adds an optional criterion without passing null to Spring Data's
     * {@link Specification#and(Specification)} method.
     *
     * @param base mandatory composed specification
     * @param optional optional filter specification
     * @return the base specification, optionally extended with the filter
     */
    private static Specification<Budget> andIfPresent(
            final Specification<Budget> base,
            final Specification<Budget> optional
    ) {
        return optional == null ? base : base.and(optional);
    }

    private static Specification<Budget> ownedBy(final UUID userId) {
        return (root, query, builder) -> builder.equal(
                root.get("user").get("id"), userId);
    }

    private static Specification<Budget> nameContains(final String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        return (root, query, builder) -> builder.like(
                builder.lower(root.get("name")), "%" + normalized + "%");
    }

    private static Specification<Budget> walletIdEquals(final UUID walletId) {
        return walletId == null ? null : (root, query, builder) ->
                builder.equal(root.get("wallet").get("id"), walletId);
    }

    private static Specification<Budget> periodEquals(
            final bflow.budget.enums.PeriodType period) {
        return period == null ? null : (root, query, builder) ->
                builder.equal(root.get("period"), period);
    }

    private static Specification<Budget> scopeEquals(
            final bflow.budget.enums.BudgetScope scope) {
        return scope == null ? null : (root, query, builder) ->
                builder.equal(root.get("scope"), scope);
    }

    private static Specification<Budget> startDateOnOrAfter(
            final LocalDate startDate) {
        return startDate == null ? null : (root, query, builder) ->
                builder.greaterThanOrEqualTo(root.get("startDate"), startDate);
    }

    private static Specification<Budget> startDateOnOrBefore(
            final LocalDate startDate) {
        return startDate == null ? null : (root, query, builder) ->
                builder.lessThanOrEqualTo(root.get("startDate"), startDate);
    }
}
