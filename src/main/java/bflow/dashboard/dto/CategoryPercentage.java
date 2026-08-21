package bflow.dashboard.dto;

import java.util.UUID;

public record CategoryPercentage(
        UUID categoryId,
        String categoryName,
        Double percentage
) { }
