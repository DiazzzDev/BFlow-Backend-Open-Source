ALTER TABLE incomes
    ADD COLUMN recurring_transaction_id UUID NULL
        REFERENCES recurring_transactions (id) ON DELETE SET NULL;

ALTER TABLE expenses
    ADD COLUMN recurring_transaction_id UUID NULL
        REFERENCES recurring_transactions (id) ON DELETE SET NULL;