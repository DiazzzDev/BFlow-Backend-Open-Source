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
import org.hibernate.annotations.UpdateTimestamp;

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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stored_file_id", nullable = false, updatable = false)
    private StoredFile storedFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, updatable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReceiptStatus status;

    /** What Textract proposes. Nothing here is final until the user
     * confirms; the frontend shows it as an editable draft. */
    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_type")
    private ReceiptTransactionType suggestedType;

    @Column(name = "suggested_title")
    private String suggestedTitle;

    @Column(name = "suggested_amount")
    private BigDecimal suggestedAmount;

    @Column(name = "suggested_category_id")
    private UUID suggestedCategoryId;

    @Column(name = "suggested_date")
    private LocalDate suggestedDate;

    @Column(name = "confidence_score")
    private BigDecimal confidenceScore;

    @Column(name = "raw_ocr_payload", columnDefinition = "jsonb")
    private String rawOcrPayload;

    /** Populated only once the user confirms. */
    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_transaction_type")
    private ReceiptTransactionType resultingTransactionType;

    @Column(name = "resulting_transaction_id")
    private UUID resultingTransactionId;

    /** Populated when status transitions to FAILED. */
    @Column(name = "failure_reason")
    private String failureReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
