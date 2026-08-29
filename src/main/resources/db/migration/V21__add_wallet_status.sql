ALTER TABLE wallets
    ADD COLUMN status VARCHAR(20);

UPDATE wallets
SET status = 'ACTIVE'
WHERE status IS NULL;

ALTER TABLE wallets
    ALTER COLUMN status SET NOT NULL;

ALTER TABLE wallets
    ADD CONSTRAINT chk_wallet_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'));