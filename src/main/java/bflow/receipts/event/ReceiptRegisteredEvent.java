package bflow.receipts.event;

import java.util.UUID;

/**
 * Raised when a {@code ReceiptUpload} is created and ready for OCR
 * processing. Published synchronously inside the same transaction
 * that persists the receipt; the actual SQS publish only happens
 * after that transaction commits (see {@code
 * bflow.receipts.messaging.ReceiptOcrRequestEventListener}), so a
 * rolled-back registration never enqueues a stray OCR request.
 *
 * @param receiptId the id of the receipt ready for OCR
 */
public record ReceiptRegisteredEvent(UUID receiptId) {
}
