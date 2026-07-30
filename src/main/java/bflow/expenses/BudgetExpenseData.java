package bflow.expenses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Minimal read model used when calculating a page of budgets.
 */
public interface BudgetExpenseData {

    /** @return the expense wallet identifier. */
    UUID getWalletId();

    /** @return the expense category identifier, when present. */
    UUID getCategoryId();

    /** @return the expense date. */
    LocalDate getDate();

    /** @return the expense amount. */
    BigDecimal getAmount();
}
