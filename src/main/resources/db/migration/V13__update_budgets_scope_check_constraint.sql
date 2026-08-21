ALTER TABLE budgets
    DROP CONSTRAINT budgets_scope_check;

ALTER TABLE budgets
    ADD CONSTRAINT budgets_scope_check
    CHECK (
        (scope)::text = ANY (
            (ARRAY['WALLET', 'WALLET_CATEGORY', 'CATEGORY_GLOBAL'])::text[]
        )
    );