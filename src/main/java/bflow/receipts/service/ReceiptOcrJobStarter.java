package bflow.receipts.service;

import bflow.receipts.entity.ReceiptUpload;
import bflow.receipts.enums.ReceiptStatus;
import bflow.receipts.repository.RepositoryReceiptUpload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.DocumentLocation;
import software.amazon.awssdk.services.textract.model.NotificationChannel;
import software.amazon.awssdk.services.textract.model.S3Object;
import software.amazon.awssdk.services.textract.model.StartExpenseAnalysisRequest;
import software.amazon.awssdk.services.textract.model.StartExpenseAnalysisResponse;

import java.util.UUID;

/**
 * Submits the async Textract job for a receipt. Called by {@code
 * ReceiptOcrRequestListener} after popping a message off the OCR
 * request queue.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptOcrJobStarter {

    /** Repository for receipt upload records. */
    private final RepositoryReceiptUpload repositoryReceiptUpload;

    /** Persists the PROCESSING transition in its own transaction. */
    private final ReceiptStatusTransitionService statusTransitionService;

    /** Textract client used to submit the job. */
    private final TextractClient textractClient;

    /** Bucket the receipt's underlying file lives in. */
    @Value("${aws.s3.bucket}")
    private String bucket;

    /** SNS topic Textract publishes the completion notification to. */
    @Value("${aws.sns.receipt-ocr-results-topic-arn}")
    private String resultsTopicArn;

    /**
     * IAM role Textract assumes to publish to {@link
     * #resultsTopicArn}. Must trust {@code textract.amazonaws.com}
     * and have {@code sns:Publish} on that topic — see {@code
     * infra/textract-ocr/02-configure-iam.sh}.
     */
    @Value("${aws.textract.sns-role-arn}")
    private String textractSnsRoleArn;

    /**
     * Starts an {@code AnalyzeExpense} job for the given receipt and
     * records the returned JobId.
     *
     * <p>Idempotent at the business level: if the receipt is no
     * longer {@code RECEIVED} (e.g. this is a redelivered SQS
     * message for a receipt already submitted), the call is a
     * no-op. Idempotent at the Textract level too: {@code
     * clientRequestToken} is the receipt id, so even a genuine
     * double-submit within Textract's dedup window does not create
     * a second job.</p>
     *
     * @param receiptId the id of the receipt to process
     * @throws IllegalStateException if the receipt does not exist
     */
    public void startJob(final UUID receiptId) {
        ReceiptUpload receipt = repositoryReceiptUpload
                .findByIdWithStoredFile(receiptId)
                .orElseThrow(() -> new IllegalStateException(
                        "Receipt upload not found: " + receiptId));

        if (receipt.getStatus() != ReceiptStatus.RECEIVED) {
            log.info(
                    "Skipping OCR request for receipt {}: status is "
                            + "already {}",
                    receiptId, receipt.getStatus()
            );
            return;
        }

        String objectKey = receipt.getStoredFile().getObjectKey();

        StartExpenseAnalysisRequest request = StartExpenseAnalysisRequest
                .builder()
                .documentLocation(DocumentLocation.builder()
                        .s3Object(S3Object.builder()
                                .bucket(bucket)
                                .name(objectKey)
                                .build())
                        .build())
                .notificationChannel(NotificationChannel.builder()
                        .snsTopicArn(resultsTopicArn)
                        .roleArn(textractSnsRoleArn)
                        .build())
                .clientRequestToken(receiptId.toString())
                .build();

        StartExpenseAnalysisResponse response =
                textractClient.startExpenseAnalysis(request);

        statusTransitionService.markProcessing(
                receiptId, response.jobId());

        log.info(
                "Submitted Textract job {} for receipt {}",
                response.jobId(), receiptId
        );
    }
}
