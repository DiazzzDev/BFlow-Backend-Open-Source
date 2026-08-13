ALTER TABLE recurring_transactions
    ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_failure_reason VARCHAR(150),
    ADD COLUMN last_failure_at TIMESTAMP;