package bflow.wallet.DTO;

import bflow.wallet.enums.WalletRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Represents a member of a shared wallet.
 */
@Getter
@AllArgsConstructor
public class WalletMemberResponse {

    /**
     * Unique identifier of the wallet member.
     */
    private UUID id;

    /**
     * Email address of the user.
     */
    private String email;

    /**
     * Role assigned to the user within the wallet.
     */
    private WalletRole role;

    /**
     * Date and time when the user joined the wallet.
     */
    private Instant joinedAt;
}
