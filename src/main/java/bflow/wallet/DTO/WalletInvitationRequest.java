package bflow.wallet.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Request used to invite a user to a wallet.
 */
@Getter
@Setter
public final class WalletInvitationRequest {

    /**
     * Email address of the invited user.
     */
    @NotBlank(message = "Invited email is required.")
    @Email(message = "Invalid email address.")
    private String invitedEmail;
}
