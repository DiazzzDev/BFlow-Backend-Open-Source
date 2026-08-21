UPDATE budgets
SET scope = 'WALLET_CATEGORY'
WHERE scope = 'CATEGORY';

ALTER TABLE budgets
    ALTER COLUMN wallet_id DROP NOT NULL;

ALTER TABLE budgets
    ADD CONSTRAINT chk_budget_scope_wallet_consistency
        CHECK (
            (scope = 'CATEGORY_GLOBAL' AND wallet_id IS NULL)
                OR (scope != 'CATEGORY_GLOBAL' AND wallet_id IS NOT NULL)
            );

CREATE UNIQUE INDEX idx_budget_unique_wallet_category
    ON budgets(wallet_id, user_id, category_id, period)
    WHERE scope = 'WALLET_CATEGORY';

CREATE UNIQUE INDEX idx_budget_unique_wallet
    ON budgets(wallet_id, user_id, period)
    WHERE scope = 'WALLET';

CREATE UNIQUE INDEX idx_budget_unique_category_global
    ON budgets(user_id, category_id, period)
    WHERE scope = 'CATEGORY_GLOBAL';