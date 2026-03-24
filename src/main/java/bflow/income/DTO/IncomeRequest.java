package bflow.income.DTO;

import bflow.common.financial.BaseTransactionRequest;
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
     * Whether this income is taxable.
     */
    @NotNull
    private Boolean taxable = false;
}
