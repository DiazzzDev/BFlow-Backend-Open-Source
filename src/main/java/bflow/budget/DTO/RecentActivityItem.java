package bflow.budget.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single recent activity entry (transaction) shown in the budget detail.
 */
@Getter
@AllArgsConstructor
public class RecentActivityItem {

    /** Transaction identifier. */
    private UUID id;

    /** Transaction description. */
    private String description;

    /** Date when the transaction occurred. */
    private LocalDate date;

    /** Transaction amount. */
    private BigDecimal amount;
}
