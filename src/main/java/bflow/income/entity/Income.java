package bflow.income.entity;

import bflow.common.financial.FinancialEntry;
import bflow.income.enums.IncomeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing incomes in wallets.
 */
@Entity
@Table(name = "incomes")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Income extends FinancialEntry {

    /**
     * The type of income (e.g., SALARY, BONUS, FREELANCE).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncomeType type;

    /**
     * Whether this income is taxable.
     */
    @Column
    private Boolean taxable;

    /**
     * Whether this income is a recurring income.
     */
    @Column
    private Boolean recurring;

    /**
     * The recurrence pattern for recurring income (e.g., MONTHLY, YEARLY).
     */
    @Column
    private String recurrencePattern;
}
