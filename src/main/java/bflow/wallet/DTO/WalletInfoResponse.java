package bflow.wallet.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * BFF-optimized response for the wallet "Information" panel
 * (last activity, highest expense, upcoming recurring items, etc).
 */
@Getter
@AllArgsConstructor
public class WalletInfoResponse {

    /**
     * Most recent transaction timestamp
     * across incomes/expenses/transfers. Null if empty.
     */
    private final Instant lastActivity;

    /**
     * Title of the single highest-amount expense in this wallet.
     * Null if none.
     */
    private final String highestExpense;

    /** Total count of incomes + expenses + transfers in this wallet. */
    private final long transactions;

    /** The wallet's initial value. */
    private final BigDecimal initialValue;

    /** Top 3 upcoming active recurring transactions, nearest first. */
    private final List<UpcomingTransactionResponse> upcoming;
}
