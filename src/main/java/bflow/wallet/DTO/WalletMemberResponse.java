package bflow.wallet.DTO;

import bflow.wallet.enums.WalletRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class WalletMemberResponse {

    private UUID id;

    private String email;

    private WalletRole role;

    private Instant joinedAt;
}
