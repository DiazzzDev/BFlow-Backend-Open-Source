package bflow.wallet.DTO;

import lombok.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWalletsResponse {
    /** The user associated with the wallets. */
    private UUID userId;
    private String userEmail;

    private List<WalletResponse> wallets;
}
