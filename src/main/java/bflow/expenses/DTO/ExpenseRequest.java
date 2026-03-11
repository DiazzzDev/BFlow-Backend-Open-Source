package bflow.expenses.DTO;

import bflow.expenses.enums.ExpenseType;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for creating or updating expense entries.
 */
@Getter
@Setter
public class ExpenseRequest {

    /** Minimum length for expense title. */
    private static final int TITLE_MIN_LENGTH = 5;
    /** Maximum length for expense title. */
    private static final int TITLE_MAX_LENGTH = 50;
    /** Maximum length for expense description. */
    private static final int DESCRIPTION_MAX_LENGTH = 100;
    /** Maximum integer digits for expense amount. */
    private static final int AMOUNT_INTEGER_DIGITS = 15;
    /** Fraction digits for expense amount (decimal places). */
    private static final int AMOUNT_FRACTION_DIGITS = 2;

    /** The title of the expense. */
    @NotBlank
    @Size(min = TITLE_MIN_LENGTH, max = TITLE_MAX_LENGTH)
    private String title;

    /** Optional description of the expense. */
    @Nullable
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    /** The amount of the expense. */
    @Digits(integer = AMOUNT_INTEGER_DIGITS,
            fraction = AMOUNT_FRACTION_DIGITS)
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    /** The date when the expense occurred. */
    @NotNull
    private LocalDate date;

    /** The ID of the wallet associated with this expense. */
    @NotNull
    private UUID walletId;

    /** The type/category of the expense. */
    @NotNull
    private ExpenseType type;

    /** Indicates whether the expense is tax deductible. */
    private Boolean taxDeductible = false;

    /** Indicates whether the expense is recurring. */
    private Boolean recurring = false;

    /** The recurrence pattern if the expense is recurring. */
    private String recurrencePattern;

    /** Indicates whether the expense is reimbursable. */
    private Boolean reimbursable = false;

}
