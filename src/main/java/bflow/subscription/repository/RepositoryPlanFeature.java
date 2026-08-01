package bflow.subscription.repository;

import bflow.subscription.entities.PlanFeature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RepositoryPlanFeature extends JpaRepository<PlanFeature, UUID> {
    Optional<PlanFeature> findByPlanIdAndFeatureCode(UUID planId, String featureCode);

    List<PlanFeature> findByPlanId(UUID planId);
}
