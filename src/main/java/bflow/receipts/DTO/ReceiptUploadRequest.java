package bflow.receipts.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/** Registers a previously uploaded file as a receipt for a wallet. */
@Getter
@Setter
public class ReceiptUploadRequest {

    /** Identifier of the previously uploaded file. */
    @NotNull(message = "The file id is required")
    private UUID fileId;

    /** Identifier of the wallet associated with the receipt. */
    @NotNull(message = "The wallet id is required")
    private UUID walletId;
}
