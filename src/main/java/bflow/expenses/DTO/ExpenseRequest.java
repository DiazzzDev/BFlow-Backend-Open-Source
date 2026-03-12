package bflow.expenses.DTO;

import bflow.common.financial.BaseTransactionRequest;
import bflow.expenses.enums.ExpenseType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for creating or updating expense entries.
 */
@Getter
@Setter
public class ExpenseRequest extends BaseTransactionRequest {

    /** The type/category of the expense. */
    @NotNull(message = "Expense type is required")
    private ExpenseType type;

    /** Indicates whether the expense is tax deductible. */
    @NotNull
    private Boolean taxDeductible = false;

    /** Indicates whether the expense is reimbursable. */
    @NotNull
    private Boolean reimbursable = false;

}
