INSERT INTO features (id, code, name, limitable) VALUES
    (gen_random_uuid(), 'BUDGETS', 'Presupuestos', true),
    (gen_random_uuid(), 'WALLETS', 'Billeteras', true),
    (gen_random_uuid(), 'SHARED_WALLETS', 'Billeteras compartidas', true),
    (gen_random_uuid(), 'WALLET_MEMBERS', 'Miembros por billetera', true),
    (gen_random_uuid(), 'RECURRING_TRANSACTIONS', 'Transacciones recurrentes', true),
    (gen_random_uuid(), 'DASHBOARD_CUSTOMIZATION', 'Personalización de dashboard', false),
    (gen_random_uuid(), 'EXPORT', 'Exportar datos', false),
    (gen_random_uuid(), 'CAN_CREATE_SHARED_WALLETS', 'Crear billeteras compartidas', false);

-- Límites numéricos: migrar desde las columnas max_* de plans
INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_budgets, true
FROM plans p, features f WHERE f.code = 'BUDGETS';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_wallets, true
FROM plans p, features f WHERE f.code = 'WALLETS';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_shared_wallets, true
FROM plans p, features f WHERE f.code = 'SHARED_WALLETS';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_wallet_members, true
FROM plans p, features f WHERE f.code = 'WALLET_MEMBERS';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, p.max_recurring_transactions, true
FROM plans p, features f WHERE f.code = 'RECURRING_TRANSACTIONS';

-- Toggles booleanos: limit queda NULL, enabled = la columna boolean existente
INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.dashboard_customization
FROM plans p, features f WHERE f.code = 'DASHBOARD_CUSTOMIZATION';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.export_enabled
FROM plans p, features f WHERE f.code = 'EXPORT';

INSERT INTO plan_features (id, plan_id, feature_id, "limit", enabled)
SELECT gen_random_uuid(), p.id, f.id, NULL, p.can_create_shared_wallets
FROM plans p, features f WHERE f.code = 'CAN_CREATE_SHARED_WALLETS';