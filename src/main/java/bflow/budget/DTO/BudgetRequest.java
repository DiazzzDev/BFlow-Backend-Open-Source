package bflow.budget.DTO;

import bflow.budget.enums.BudgetScope;
import bflow.budget.enums.PeriodType;
import bflow.wallet.enums.Currency;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for budget creation request.
 */
@Getter
@Setter
@NoArgsConstructor
public class BudgetRequest {

    /**
     * Threshold validation maximum.
     */
    private static final int THRESHOLD_MAX = 99;

    /**
     * Warning threshold default value.
     */
    private static final int WARNING_THRESHOLD_DEFAULT = 70;

    /**
     * Critical threshold default value.
     */
    private static final int CRITICAL_THRESHOLD_DEFAULT = 90;

    /**
     * The wallet ID for the budget. Required for WALLET and
     * WALLET_CATEGORY scopes; must be omitted for CATEGORY_GLOBAL.
     */
    private UUID walletId;

    /**
     * The budget amount.
     */
    @NotNull
    @Positive
    private BigDecimal amount;

    /**
     * The budget period type.
     */
    @NotNull
    private PeriodType period;

    /**
     * The budget start date.
     */
    @NotNull
    private LocalDate startDate;

    /**
     * The category ID (required if scope is CATEGORY).
     */
    private UUID categoryId;

    /**
     * The warning threshold percentage.
     */
    @Min(1)
    @Max(THRESHOLD_MAX)
    private Integer thresholdWarning = WARNING_THRESHOLD_DEFAULT;

    /**
     * The critical threshold percentage.
     */
    @Min(1)
    @Max(THRESHOLD_MAX)
    private Integer thresholdCritical = CRITICAL_THRESHOLD_DEFAULT;

    /**
     * The budget scope (WALLET or CATEGORY).
     */
    @NotNull
    private BudgetScope scope;

    /**
     * The currency this budget's amount is denominated in. For
     * WALLET/WALLET_CATEGORY scope it must match the target
     * wallet's own currency. For CATEGORY_GLOBAL scope, this
     * declares which currency is being tracked — only wallets in
     * this currency are included when calculating spend, since
     * amounts in different currencies cannot be summed directly.
     */
    @NotNull
    private Currency currency;
}
