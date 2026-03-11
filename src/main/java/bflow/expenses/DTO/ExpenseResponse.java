package bflow.expenses.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for expense response in API calls.
 */
@Getter
@Setter
public class ExpenseResponse {

    /** The unique identifier for the expense. */
    private String id;

    /** The title of the expense. */
    private String title;

    /** The description of the expense. */
    private String description;

    /** The amount of the expense. */
    private BigDecimal amount;

    /** The date when the expense occurred. */
    private LocalDate date;

    /** The type/category of the expense. */
    private String expenseType;

    /** Flag indicating if the expense is tax deductible. */
    private Boolean taxDeductible;

    /** Flag indicating if the expense is recurring. */
    private Boolean recurring;

    /** Flag indicating if the expense is reimbursable. */
    private Boolean reimbursable;

    /** The ID of the wallet containing this expense. */
    private String walletId;

    /** The name of the wallet containing this expense. */
    private String walletName;

    /** The ID of the user who contributed this expense. */
    private String contributorId;

    /** The name of the user who contributed this expense. */
    private String contributorName;

    /** The timestamp when the expense was created. */
    private Instant createdAt;

}
