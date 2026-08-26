package bflow.receipts.messaging;

import bflow.receipts.service.ReceiptOcrJobStarter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.UUID;

/**
 * Polls the receipt OCR request queue and submits a Textract job for
 * each receipt id it finds, via {@link ReceiptOcrJobStarter}.
 *
 * <p>Messages are published by {@code ReceiptOcrRequestEventListener}
 * right after a {@code ReceiptUpload} is committed.</p>
 */
@Slf4j
@Component
public class ReceiptOcrRequestListener extends AbstractSqsPollingWorker {

    /** Service that submits the actual Textract job. */
    private final ReceiptOcrJobStarter receiptOcrJobStarter;

    /**
     * Creates the listener and wires it to the request queue.
     *
     * @param sqsClient the SQS client to poll with
     * @param receiptOcrJobStarter service that submits the Textract
     *         job for a receipt
     * @param queueUrl URL of the OCR request queue
     * @param waitTimeSeconds long-poll wait time, in seconds
     * @param maxMessages maximum messages per receive call
     * @param errorBackoffSeconds pause after a poll failure, in
     *         seconds
     */
    public ReceiptOcrRequestListener(
            final SqsClient sqsClient,
            final ReceiptOcrJobStarter receiptOcrJobStarter,
            @Value("${aws.sqs.receipt-ocr-requests-queue-url}")
            final String queueUrl,
            @Value("${app.ocr.request-poll-wait-seconds}")
            final int waitTimeSeconds,
            @Value("${app.ocr.poll-max-messages}")
            final int maxMessages,
            @Value("${app.ocr.poll-error-backoff-seconds}")
            final int errorBackoffSeconds
    ) {
        super(sqsClient, queueUrl, waitTimeSeconds, maxMessages,
                errorBackoffSeconds);
        this.receiptOcrJobStarter = receiptOcrJobStarter;
    }

    @Override
    protected String workerName() {
        return "receipt-ocr-request-listener";
    }

    @Override
    protected boolean handle(final Message message) {
        UUID receiptId;
        try {
            receiptId = UUID.fromString(message.body());
        } catch (IllegalArgumentException ex) {
            log.error(
                    "Malformed receipt id in OCR request message {}: '{}'",
                    message.messageId(), message.body(), ex
            );
            // Not recoverable by retrying — drop it.
            return true;
        }

        receiptOcrJobStarter.startJob(receiptId);
        return true;
    }
}
