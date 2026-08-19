package bflow.receipts.DTO;

import bflow.receipts.enums.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ReceiptUploadResponse {
    private final UUID id;
    private final UUID fileId;
    private final UUID walletId;
    private final ReceiptStatus status;
    private final UUID resultingExpenseId;
    private final Instant createdAt;
}
