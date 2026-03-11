package bflow.wallet.DTO;

import bflow.wallet.enums.Currency;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    /** The unique identifier of the wallet. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;

    /** The display name of the wallet. */
    @NotBlank(message = "Wallet name is required")
    private String name;

    /** The description of the wallet. */
    @NotBlank(message = "Wallet description is required")
    private String description;

    /** The currency of the wallet. */
    @NotNull(message = "Wallet currency is required")
    private Currency currency;

    /** The starting balance when the wallet was created. */
    @NotNull(message = "Initial value is required")
    private BigDecimal initialValue;
}
