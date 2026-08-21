package bflow.dashboard.dto;

import java.math.BigDecimal;

public record BalanceSummaryResponse(
        BigDecimal total,
        Double percentageChangeLastMonth
) { }
