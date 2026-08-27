-- Limits below are intentionally capped, not "unlimited", per
-- product decision: generous enough that no real user hits them
-- organically, low enough to bound resource abuse.

INSERT INTO plans (
    id,
    code,
    name,
    price,
    billing_period,
    max_wallets,
    max_budgets,
    max_recurring_transactions,
    max_shared_wallets,
    max_wallet_members,
    dashboard_customization,
    can_create_shared_wallets,
    export_enabled,
    active,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'PRO',
    'Pro Plan',
    9.99,              -- TODO: confirm real price
    'MONTHLY',
    100,                -- wallets: capped, not literally unlimited
    100,                -- budgets: capped, not literally unlimited
    25,                 -- recurring transactions
    100,                -- shared wallets (not currently enforced — see note below)
    10,                 -- members per shared wallet (invite + administer)
    true,
    true,
    true,
    true,
    NOW(),
    NOW()
)
ON CONFLICT (code) DO NOTHING;

-- Mirrors the plan_features rows V10 generated for FREE, but for
-- the new PRO plan — this is the table PlanLimitService actually
-- reads; the max_* columns on `plans` above are legacy and no
-- longer enforced directly.
INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_budgets, true
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'BUDGETS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_wallets, true
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'WALLETS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_shared_wallets, true
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'SHARED_WALLETS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_wallet_members, true
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'WALLET_MEMBERS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_recurring_transactions, true
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'RECURRING_TRANSACTIONS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.dashboard_customization
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'DASHBOARD_CUSTOMIZATION'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.export_enabled
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'EXPORT'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.can_create_shared_wallets
FROM plans p, features f
WHERE p.code = 'PRO' AND f.code = 'CAN_CREATE_SHARED_WALLETS'
ON CONFLICT ON CONSTRAINT uq_plan_feature DO NOTHING;