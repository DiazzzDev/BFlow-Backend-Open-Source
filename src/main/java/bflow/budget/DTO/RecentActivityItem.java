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
    private UUID id;
    private String description;
    private LocalDate date;
    private BigDecimal amount;
}