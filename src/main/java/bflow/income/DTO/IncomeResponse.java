package bflow.income.DTO;

import bflow.category.DTO.CategoryResponse;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Data Transfer Object for income responses.
 */
@Getter
@Setter
public class IncomeResponse {

    /**
     * The unique identifier of the income.
     */
    private String id;

    /**
     * The title or name of the income source.
     */
    private String title;

    /**
     * A detailed description of the income.
     */
    private String description;

    /**
     * The monetary amount of the income.
     */
    private BigDecimal amount;

    /**
     * The date when the income was received.
     */
    private LocalDate date;

    /**
     * The type of income (e.g., SALARY, BONUS).
     */
    private CategoryResponse category;

    /**
     * The unique identifier of the associated wallet.
     */
    private String walletId;

    /**
     * The name of the associated wallet.
     */
    private String walletName;

    /**
     * The unique identifier of the income contributor.
     */
    private String contributorId;

    /**
     * The name of the income contributor.
     */
    private String contributorName;

    /**
     * The source of the income (e.g., manual, receipt, voice, import).
     */
    private String source;

    /**
     * The timestamp when the income was created.
     */
    private Instant createdAt;

    /**
     * The number of times this income entry has been edited.
     */
    private Integer editCount;

    /**
     * The number of times the income category has been changed.
     */
    private Integer categorizationChanges;

    /**
     * The confidence score for the income categorization.
     */
    private Double confidenceScore;
}

