package bflow.receipts.service;

import bflow.receipts.entity.ReceiptUpload;
import bflow.receipts.enums.ReceiptStatus;
import bflow.receipts.enums.ReceiptTransactionType;
import bflow.receipts.repository.RepositoryReceiptUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Persists {@link ReceiptUpload} status transitions driven by the
 * async OCR pipeline, each in its own independent transaction.
 *
 * <p>Kept as a separate bean — not private methods on the polling
 * listeners — for the same reason as {@code
 * bflow.storage.service.FileStatusTransitionService}: {@code
 * REQUIRES_NEW} only applies through Spring's transactional proxy,
 * which self-invocation bypasses. This also means one message's
 * failure can never roll back another message's already-committed
 * transition, since each poll loop iteration is its own call into
 * this bean.</p>
 */
@Service
@RequiredArgsConstructor
public class ReceiptStatusTransitionService {

    /** Repository for receipt upload records. */
    private final RepositoryReceiptUpload repositoryReceiptUpload;

    /**
     * Transitions a receipt to {@code PROCESSING} and records the
     * Textract job that was just submitted for it.
     *
     * @param receiptId the receipt upload identifier
     * @param textractJobId the JobId returned by StartExpenseAnalysis
     * @return the updated receipt upload
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReceiptUpload markProcessing(
            final UUID receiptId, final String textractJobId
    ) {
        ReceiptUpload receipt = getOrThrow(receiptId);
        receipt.setStatus(ReceiptStatus.PROCESSING);
        receipt.setTextractJobId(textractJobId);
        return repositoryReceiptUpload.save(receipt);
    }

    /**
     * Transitions a receipt to {@code EXTRACTED} with Textract's
     * suggestion, ready for the user to review and confirm.
     *
     * @param receiptId the receipt upload identifier
     * @param extraction the mapped suggestion produced from
     *         Textract's {@code GetExpenseAnalysis} response
     * @return the updated receipt upload
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReceiptUpload markExtracted(
            final UUID receiptId, final ReceiptOcrExtraction extraction
    ) {
        ReceiptUpload receipt = getOrThrow(receiptId);
        receipt.setStatus(ReceiptStatus.EXTRACTED);
        receipt.setSuggestedType(extraction.suggestedType());
        receipt.setSuggestedTitle(extraction.suggestedTitle());
        receipt.setSuggestedAmount(extraction.suggestedAmount());
        receipt.setSuggestedDate(extraction.suggestedDate());
        receipt.setConfidenceScore(extraction.confidenceScore());
        receipt.setRawOcrPayload(extraction.rawOcrPayloadJson());
        return repositoryReceiptUpload.save(receipt);
    }

    /**
     * Transitions a receipt to {@code FAILED}, recording why.
     * Terminal state: the user must re-upload rather than retry
     * this same receipt.
     *
     * @param receiptId the receipt upload identifier
     * @param reason a short, non-sensitive explanation
     * @return the updated receipt upload
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReceiptUpload markFailed(
            final UUID receiptId, final String reason
    ) {
        ReceiptUpload receipt = getOrThrow(receiptId);
        receipt.setStatus(ReceiptStatus.FAILED);
        receipt.setFailureReason(reason);
        return repositoryReceiptUpload.save(receipt);
    }

    /**
     * Loads a receipt upload by id without an ownership check —
     * safe here because every caller is internal (SQS listeners),
     * never a request driven directly by another user's input.
     *
     * @param receiptId the receipt upload identifier
     * @return the receipt upload
     * @throws IllegalStateException if no such receipt exists
     */
    private ReceiptUpload getOrThrow(final UUID receiptId) {
        return repositoryReceiptUpload.findById(receiptId)
                .orElseThrow(() -> new IllegalStateException(
                        "Receipt upload not found: " + receiptId));
    }

    /**
     * Textract's mapped suggestion for a receipt, ready to persist.
     *
     * @param suggestedType always EXPENSE today — AnalyzeExpense has
     *         no concept of income, this is just the sensible
     *         default the user can override on confirm
     * @param suggestedTitle the vendor/merchant name, or null
     * @param suggestedAmount the total amount, or null
     * @param suggestedDate the invoice/receipt date, or null
     * @param confidenceScore average confidence of the fields used
     *         above, or null
     * @param rawOcrPayloadJson every summary field Textract
     *         detected, as JSON — not the raw SDK response (its
     *         POJOs aren't JavaBean-shaped, so Jackson can't dump
     *         them directly), but a plain, always-serializable
     *         reconstruction the frontend can use to show fields
     *         beyond the three mapped above
     */
    public record ReceiptOcrExtraction(
            ReceiptTransactionType suggestedType,
            String suggestedTitle,
            BigDecimal suggestedAmount,
            LocalDate suggestedDate,
            BigDecimal confidenceScore,
            String rawOcrPayloadJson
    ) {
    }
}
