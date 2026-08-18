package bflow.wallet.DTO;

import bflow.recurring.enums.RecurringType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A single item in the wallet's "Upcoming" recurring transactions list. */
@Getter
@AllArgsConstructor
public class UpcomingTransactionResponse {

    /** Title of the recurring transaction (e.g. "Netflix"). */
    private final String title;

    private final BigDecimal amount;

    private final RecurringType type;

    /** The next date this recurring transaction is due. */
    private final LocalDate nextExecutionDate;
}
