package bflow.wallet.DTO;

import bflow.recurring.enums.RecurringType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single item in the wallet's "Upcoming" recurring transactions list.
 */
@Getter
@AllArgsConstructor
public class UpcomingTransactionResponse {

    /** Title of the recurring transaction (e.g. "Netflix"). */
    private final String title;

     /** Monetary amount of the recurring transaction. */
    private final BigDecimal amount;

    /** Type of the recurring transaction, such as income or expense. */
    private final RecurringType type;

    /** The next date this recurring transaction is due. */
    private final LocalDate nextExecutionDate;
}
