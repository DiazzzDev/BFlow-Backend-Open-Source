package bflow.receipts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

/**
 * Publishes a request to run OCR on a just-registered receipt.
 *
 * <p>The message body only carries the receipt's id — the consumer
 * (see {@code bflow.receipts.messaging.ReceiptOcrRequestListener})
 * resolves the S3 object key and everything else by loading the
 * {@code ReceiptUpload} record itself, so there is no risk of the
 * queued message and the database record drifting apart.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOcrRequestPublisher {

    /** SQS client used to send the request. */
    private final SqsClient sqsClient;

    /** URL of the queue the OCR request listener polls. */
    @Value("${aws.sqs.receipt-ocr-requests-queue-url}")
    private String requestsQueueUrl;

    /**
     * Enqueues an OCR request for the given receipt.
     *
     * <p>Called at the end of {@code ReceiptUploadService#register},
     * after the {@code ReceiptUpload} row is committed. If this call
     * fails, the receipt stays in {@code RECEIVED} — the frontend's
     * status poll simply won't advance, which is a safe failure
     * mode (no partial or duplicate processing) that a background
     * reconciliation job can pick up later; that job is intentionally
     * left out of this first pass.</p>
     *
     * @param receiptId the id of the receipt to process
     */
    public void publishOcrRequest(final UUID receiptId) {
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(requestsQueueUrl)
                    .messageBody(receiptId.toString())
                    .build());
        } catch (RuntimeException ex) {
            log.error(
                    "Failed to publish OCR request for receipt {}",
                    receiptId, ex
            );
        }
    }
}
