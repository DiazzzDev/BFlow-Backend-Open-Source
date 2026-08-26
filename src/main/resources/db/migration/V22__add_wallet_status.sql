ALTER TABLE wallets
DROP CONSTRAINT chk_wallet_status;

ALTER TABLE wallets
DROP COLUMN status;