package bflow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record SpendingSummaryResponse(
        BigDecimal totalSpent,
        Double totalActivityPercentage,
        List<CategoryPercentage> topCategories
) { }
