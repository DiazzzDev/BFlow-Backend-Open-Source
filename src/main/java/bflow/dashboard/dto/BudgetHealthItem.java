package bflow.dashboard.dto;

import bflow.budget.enums.BudgetStatus;

import java.time.Instant;
import java.util.UUID;

public record BudgetHealthItem(
        UUID id,
        String displayName,
        Instant updatedAt,
        BudgetStatus status
) { }
