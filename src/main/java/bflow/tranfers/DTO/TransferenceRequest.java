package bflow.tranfers.DTO;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for transfer request data.
 */
@Getter
@Setter
public class TransferenceRequest {
    /** The UUID of the source wallet. */
    @NotNull(message = "Source wallet id is required")
    private UUID fromWalletId;

    /** The UUID of the destination wallet. */
    @NotNull(message = "Destination wallet id is required")
    private UUID toWalletId;

    /** The transfer amount. */
    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @DecimalMax(value = "1000000000.00", message = "Amount exceeds maximum allowed")
    @Digits(integer = 12, fraction = 2, message = "Amount must have up to 12 digits and 2 decimals")
    private BigDecimal amount;

    /** Optional transfer description. */
    @Size(max = 255, message = "Description must not exceed 255 characters")
    @Pattern(
            regexp = "^[\\p{L}0-9 .,'\\-()]*$",
            message = "Description contains invalid characters"
    )
    private String description;

}
