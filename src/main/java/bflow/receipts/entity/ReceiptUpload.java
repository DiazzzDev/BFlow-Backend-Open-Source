package bflow.receipts.entity;

import bflow.auth.entities.User;
import bflow.receipts.enums.ReceiptStatus;
import bflow.receipts.enums.ReceiptTransactionType;
import bflow.storage.entity.StoredFile;
import bflow.wallet.entities.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bridges an uploaded receipt image to a wallet before an Expense or
 * Income exists. Created the moment a user picks a wallet for a
 * photo they just uploaded; holds Textract's suggestion for review,
 * then the confirmed result once the user accepts it — at which
 * point {@code resultingTransactionType}/{@code resultingTransactionId}
 * point at the Expense or Income that was created, and that entity's
 * own {@code receiptFileId} becomes the receipt's permanent home.
 */
@Entity
@Table(name = "receipt_uploads")
@Getter
@Setter
public final class ReceiptUpload {

    /**
     * The unique identifier for this receipt upload.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /**
     * The user who uploaded this receipt.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    /**
     * The uploaded receipt image file.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stored_file_id", nullable = false, updatable = false)
    private StoredFile storedFile;

    /**
     * The wallet this receipt was uploaded against.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    /**
     * The current lifecycle status of this receipt upload.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptStatus status;

    /** What Textract proposes. Nothing here is final until the user
     * confirms; the frontend shows it as an editable draft. */
    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_type")
    private ReceiptTransactionType suggestedType;

    /**
     * Textract's suggested title for the resulting transaction.
     */
    @Column(name = "suggested_title")
    private String suggestedTitle;

    /**
     * Textract's suggested amount for the resulting transaction.
     */
    @Column(name = "suggested_amount")
    private BigDecimal suggestedAmount;

    /**
     * Textract's suggested category for the resulting transaction.
     */
    @Column(name = "suggested_category_id")
    private UUID suggestedCategoryId;

    /**
     * Textract's suggested date for the resulting transaction.
     */
    @Column(name = "suggested_date")
    private LocalDate suggestedDate;

    /**
     * Textract's confidence score for its suggestion.
     */
    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;

    /**
     * The raw OCR payload returned by Textract, stored as JSON.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "raw_ocr_payload", columnDefinition = "jsonb")
    private String rawOcrPayload;

    /**
     * The Textract {@code JobId} returned by {@code
     * StartExpenseAnalysis}. Populated as soon as the async job is
     * submitted; used to correlate the SNS completion notification
     * (which only carries the JobId) back to this receipt.
     */
    @Column(name = "textract_job_id")
    private String textractJobId;

    /**
     * The type of transaction (Expense or Income) that this receipt
     * resulted in. Populated only once the user confirms.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_transaction_type")
    private ReceiptTransactionType resultingTransactionType;

    /**
     * The identifier of the Expense or Income that this receipt
     * resulted in. Populated only once the user confirms.
     */
    @Column(name = "resulting_transaction_id")
    private UUID resultingTransactionId;

    /** Populated when status transitions to FAILED. */
    @Column(name = "failure_reason")
    private String failureReason;

    /**
     * The timestamp at which this receipt upload was created.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * The timestamp at which this receipt upload was last updated.
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
