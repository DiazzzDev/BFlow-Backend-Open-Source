package bflow.common.financial;

import bflow.auth.entities.User;
import bflow.wallet.entities.Wallet;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Abstract base class for financial entries (income and expenses).
 * Provides common fields and relationships for all financial transactions.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class FinancialEntry {
    /**
     * Maximum length for description field.
     */
    private static final int MAX_DESCRIPTION_LENGTH = 150;

    /**
     * Maximum number of integer digits for BigDecimal amount.
     */
    private static final int AMOUNT_PRECISION = 15;

    /**
     * Unique identifier for this financial entry.
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Title of this financial entry.
     */
    @Column(nullable = false)
    private String title;

    /**
     * Detailed description of this financial entry.
     */
    @Column(length = MAX_DESCRIPTION_LENGTH)
    private String description;

    /**
     * Amount of this financial entry.
     */
    @Column(nullable = false,
            precision = AMOUNT_PRECISION, scale = 2)
    private BigDecimal amount;

    /**
     * Date of this financial entry.
     */
    @Column(nullable = false)
    private LocalDate date;

    /**
     * Wallet associated with this financial entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    /**
     * User who contributed this financial entry.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User contributor;

    /**
     * Timestamp when this entry was created.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
