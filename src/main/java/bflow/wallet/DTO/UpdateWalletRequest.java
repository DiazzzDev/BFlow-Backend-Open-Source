package bflow.wallet.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request object for updating a Wallet.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWalletRequest {
    /** Minimum wallet name length. */
    private static final int NAME_MIN_LENGTH = 2;

    /** Maximum wallet name length. */
    private static final int NAME_MAX_LENGTH = 100;

    /** Minimum description length. */
    private static final int DESCRIPTION_MIN_LENGTH = 3;

    /** Maximum description length. */
    private static final int DESCRIPTION_MAX_LENGTH = 255;

    /** The display name of the wallet. */
    @NotBlank(message = "Wallet name is required")
    @Size(
            min = NAME_MIN_LENGTH,
            max = NAME_MAX_LENGTH,
            message = "Wallet name must be between 2 and 100 characters"
    )
    @Pattern(
            regexp = "^[\\p{L}0-9 .,'\\-()]+$",
            message = "Wallet name contains invalid characters"
    )
    private String name;

    /** The description of the wallet. */
    @NotBlank(message = "Wallet description is required")
    @Size(
            min = DESCRIPTION_MIN_LENGTH,
            max = DESCRIPTION_MAX_LENGTH,
            message = "Description must be between 3 and 255 characters")
    @Pattern(
            regexp = "^[\\p{L}0-9 .,'\\-()]+$",
            message = "Description contains invalid characters"
    )
    private String description;
}
