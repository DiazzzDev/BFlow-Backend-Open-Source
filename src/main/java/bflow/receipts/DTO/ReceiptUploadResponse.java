package bflow.receipts.DTO;

import bflow.receipts.enums.ReceiptStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload returned after a receipt upload is created,
 * confirmed, or fetched.
 */
@Getter
@AllArgsConstructor
public class ReceiptUploadResponse {

    /**
     * The unique identifier of the receipt upload.
     */
    private final UUID id;

    /**
     * The identifier of the uploaded receipt image file.
     */
    private final UUID fileId;

    /**
     * The identifier of the wallet this receipt was uploaded
     * against.
     */
    private final UUID walletId;

    /**
     * The current lifecycle status of the receipt upload.
     */
    private final ReceiptStatus status;

    /**
     * The identifier of the Expense or Income this receipt resulted
     * in, once confirmed; {@code null} otherwise.
     */
    private final UUID resultingExpenseId;

    /**
     * The timestamp at which the receipt upload was created.
     */
    private final Instant createdAt;
}
