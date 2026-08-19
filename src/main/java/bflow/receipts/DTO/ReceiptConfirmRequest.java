package bflow.receipts.DTO;

import bflow.receipts.enums.ReceiptTransactionType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * What the user actually confirms after reviewing OCR's suggestion.
 * The frontend pre-fills this from the receipt's suggested_* fields
 * and lets the user edit anything Textract got wrong before
 * submitting — the backend never falls back to the suggestion
 * silently, it only persists what's sent here.
 */
@Getter
@Setter
public class ReceiptConfirmRequest {

    @NotNull
    private ReceiptTransactionType type;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private UUID categoryId;

    @NotNull
    private LocalDate date;

    // Solo aplica si type == EXPENSE; se ignora si es INCOME.
    private Boolean taxDeductible;
    private Boolean reimbursable;

    // Solo aplica si type == INCOME; se ignora si es EXPENSE.
    private Boolean taxable;
}