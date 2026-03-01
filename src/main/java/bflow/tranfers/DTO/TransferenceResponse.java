package bflow.tranfers.DTO;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class TransferenceResponse {
    private UUID id;

    private UUID fromWalletId;
    private String fromWalletName;

    private UUID toWalletId;
    private String toWalletName;

    private BigDecimal amount;

    private String description;

    private String status;
}
