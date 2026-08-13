package bflow.dashboard.dto;

import java.math.BigDecimal;

public record AveragesResponse(
        BigDecimal averageIncome,
        Double incomePercentageChangeLastMonth,
        BigDecimal averageExpenses,
        Double expensesPercentageChangeLastMonth
) { }
