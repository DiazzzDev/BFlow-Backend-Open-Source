package bflow.receipts.messaging;

import bflow.receipts.event.ReceiptRegisteredEvent;
import bflow.receipts.service.ReceiptOcrRequestPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Bridges {@link ReceiptRegisteredEvent} to the OCR request queue.
 *
 * <p>{@code AFTER_COMMIT} is what makes this safe: if {@code
 * ReceiptUploadService#register} rolls back for any reason, this
 * listener never runs and no OCR request is enqueued for a receipt
 * that doesn't exist in the database.</p>
 */
@Component
@RequiredArgsConstructor
public class ReceiptOcrRequestEventListener {

    /** Publisher that actually sends the SQS message. */
    private final ReceiptOcrRequestPublisher receiptOcrRequestPublisher;

    /**
     * Enqueues the OCR request once the receipt's registration has
     * committed.
     *
     * @param event the committed registration event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReceiptRegistered(final ReceiptRegisteredEvent event) {
        receiptOcrRequestPublisher.publishOcrRequest(event.receiptId());
    }
}
