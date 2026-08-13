package bflow.dashboard.dto;

import java.util.List;

public record StatisticsResponse(
        List<MonthlyPoint> months
) { }
