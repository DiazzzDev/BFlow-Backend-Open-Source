package bflow.wallet.DTO;

import bflow.wallet.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request object for creating or updating a Wallet.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletRequest {

    /** The display name of the wallet. */
    @NotBlank(message = "Wallet name is required")
    @Size(min = 2, max = 100, message = "Wallet name must be between 2 and 100 characters")
    @Pattern(
            regexp = "^[\\p{L}0-9 .,'\\-()]+$",
            message = "Wallet name contains invalid characters"
    )
    private String name;

    /** The description of the wallet. */
    @NotBlank(message = "Wallet description is required")
    @Size(min = 3, max = 255, message = "Description must be between 3 and 255 characters")
    @Pattern(
            regexp = "^[\\p{L}0-9 .,'\\-()]+$",
            message = "Description contains invalid characters"
    )
    private String description;

    /** The currency of the wallet. */
    @NotNull(message = "Wallet currency is required")
    private Currency currency;

    /** The starting balance when the wallet was created. */
    @NotNull(message = "Initial value is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Initial value cannot be negative")
    @DecimalMax(value = "1000000000.00", message = "Initial value exceeds maximum allowed")
    @Digits(integer = 12, fraction = 2, message = "Invalid monetary format")
    private BigDecimal initialValue;
}
