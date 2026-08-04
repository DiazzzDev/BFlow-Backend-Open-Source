package bflow.wallet.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/** A single item in the wallet's "Upcoming" recurring transactions list. */
@Getter
@AllArgsConstructor
public class UpcomingTransactionResponse {

    /** Title of the recurring transaction (e.g. "Netflix"). */
    private final String title;

    /** The next date this recurring transaction is due. */
    private final LocalDate nextExecutionDate;
}
