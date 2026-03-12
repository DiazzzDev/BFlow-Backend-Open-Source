package bflow.income.DTO;

import bflow.common.financial.BaseTransactionRequest;
import bflow.income.enums.IncomeType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object for creating or updating income entries.
 */
@Getter
@Setter
public class IncomeRequest extends BaseTransactionRequest {
    /**
     * Type of income (e.g., salary, bonus, investment).
     */
    @NotNull(message = "Income type is required")
    private IncomeType type;

    /**
     * Whether this income is taxable.
     */
    @NotNull
    private Boolean taxable = false;
}
