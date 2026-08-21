package bflow.subscription.dto;

import bflow.subscription.enums.SubscriptionStatus;

import java.util.Map;

public record CurrentSubscriptionResponse(
        String planCode,
        String planName,
        SubscriptionStatus status,
        Map<String, Boolean> features,
        Map<String, Integer> limits
) { }
