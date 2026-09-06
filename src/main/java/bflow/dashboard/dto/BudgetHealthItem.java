package bflow.dashboard.dto;

import bflow.budget.enums.BudgetStatus;
import bflow.wallet.enums.Currency;

import java.time.Instant;
import java.util.UUID;

public record BudgetHealthItem(
        UUID id,
        String displayName,
        Instant updatedAt,
        BudgetStatus status,
        Currency currency
) { }
