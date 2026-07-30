-- A searchable, user-visible name is required for budget discovery.
ALTER TABLE budgets ADD COLUMN name VARCHAR(100);

-- Existing installations receive a deterministic value before the column is
-- constrained; new budgets always supply their validated request name.
UPDATE budgets
SET name = 'Budget ' || id::text
WHERE name IS NULL;

ALTER TABLE budgets ALTER COLUMN name SET NOT NULL;

-- Ownership is mandatory for every search. These indexes keep the common
-- owner/name and owner/wallet predicates efficient as data grows.
CREATE INDEX IF NOT EXISTS idx_budgets_user_name
    ON budgets (user_id, name);

CREATE INDEX IF NOT EXISTS idx_budgets_user_wallet
    ON budgets (user_id, wallet_id);