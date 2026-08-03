package bflow.subscription.services;

import bflow.common.exception.PlanLimitExceededException;
import bflow.subscription.dto.CurrentSubscriptionResponse;
import bflow.subscription.entities.PlanFeature;
import bflow.subscription.entities.Subscription;
import bflow.subscription.enums.SubscriptionStatus;
import bflow.subscription.repository.RepositoryPlanFeature;
import bflow.subscription.repository.RepositorySubscription;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanLimitService {

    /**
     * Repository for user subscriptions.
     */
    private final RepositorySubscription repositorySubscription;

    /**
     * Repository for plan feature configuration.
     */
    private final RepositoryPlanFeature repositoryPlanFeature;

    /**
     * Verifies that the user can create a new resource according to the
     * current subscription plan.
     *
     * @param userId the user UUID
     * @param featureCode the feature code
     * @param currentCount the current number of created resources
     * @throws PlanLimitExceededException if the feature is disabled or the
     *         plan limit has been reached
     */
    @Transactional(readOnly = true)
    public void assertCanCreate(
        final UUID userId,
        final String featureCode,
        final long currentCount
    ) {
        PlanFeature planFeature = resolvePlanFeature(userId, featureCode);

        if (!planFeature.isEnabled()) {
            throw new PlanLimitExceededException(
                    "Tu plan no incluye " + planFeature.getFeature().getName());
        }

        Integer limit = planFeature.getLimit();
        if (limit != null && currentCount >= limit) {
            throw new PlanLimitExceededException(
                "Alcanzaste el límite de " + limit + " "
                + planFeature.getFeature().getName()
                + " de tu plan " + planFeature.getPlan().getName()
            );
        }
    }

    /**
     * Verifies that the specified feature is enabled for the user's
     * current subscription plan.
     *
     * @param userId the user UUID
     * @param featureCode the feature code
     * @throws PlanLimitExceededException if the feature is not available
     */
    @Transactional(readOnly = true)
    public void assertFeatureEnabled(
        final UUID userId,
        final String featureCode
    ) {
        PlanFeature planFeature = resolvePlanFeature(userId, featureCode);
        if (!planFeature.isEnabled()) {
            throw new PlanLimitExceededException(
                    "Tu plan no incluye " + planFeature.getFeature().getName());
        }
    }

    private PlanFeature resolvePlanFeature(
        final UUID userId,
        final String featureCode
    ) {
        Subscription subscription = repositorySubscription
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .or(() -> repositorySubscription.findByUserIdAndStatus(
                        userId, SubscriptionStatus.PAST_DUE
                ))
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario sin suscripción activa"
                ));

        return repositoryPlanFeature
            .findByPlanIdAndFeatureCode(
                subscription.getPlan().getId(),
                featureCode
            )
            .orElseThrow(() -> new IllegalStateException(
                "Plan " + subscription.getPlan().getCode()
                + " sin configuración para " + featureCode
            ));
    }

    /**
     * Retrieves information about the user's current subscription.
     *
     * @param userId the user UUID
     * @return the current subscription information
     */
    @Transactional(readOnly = true)
    public CurrentSubscriptionResponse getCurrentSubscriptionInfo(
        final UUID userId
    ) {
        Subscription subscription = repositorySubscription
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .or(() -> repositorySubscription.findByUserIdAndStatus(
                        userId, SubscriptionStatus.PAST_DUE
                ))
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario sin suscripción activa"
                ));

        List<PlanFeature> planFeatures = repositoryPlanFeature
                .findByPlanId(subscription.getPlan().getId());

        Map<String, Boolean> features = planFeatures.stream()
                .collect(Collectors.toMap(
                        pf -> pf.getFeature().getCode(), PlanFeature::isEnabled
                ));

        Map<String, Integer> limits = planFeatures.stream()
                .filter(pf -> pf.getLimit() != null)
                .collect(Collectors.toMap(
                        pf -> pf.getFeature().getCode(), PlanFeature::getLimit
                ));

        return new CurrentSubscriptionResponse(
                subscription.getPlan().getCode(),
                subscription.getPlan().getName(),
                subscription.getStatus(),
                features,
                limits);
    }
}
