package bflow.receipts.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Registers a previously uploaded file as a receipt for a wallet. */
@Getter
@Setter
public class ReceiptUploadRequest {

    @NotNull(message = "The file id is required")
    private UUID fileId;

    @NotNull(message = "The wallet id is required")
    private UUID walletId;
}
