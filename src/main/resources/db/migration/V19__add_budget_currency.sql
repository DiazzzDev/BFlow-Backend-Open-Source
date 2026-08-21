-- Every budget must declare an explicit currency. Previously,
-- CATEGORY_GLOBAL budgets summed expense amounts across every
-- wallet a user belongs to regardless of each wallet's currency
-- (e.g. adding a 300 MXN expense to a 300 USD expense as if both
-- were the same unit), and had no currency of their own to expose
-- to the client ("300 what?"). WALLET/WALLET_CATEGORY budgets had
-- an *implicit* currency via their wallet, but never stored it.

ALTER TABLE budgets
    ADD COLUMN currency VARCHAR(3) NULL;

-- Backfill WALLET/WALLET_CATEGORY budgets from their wallet's
-- currency — unambiguous, safe to infer.
UPDATE budgets b
SET currency = w.currency
FROM wallets w
WHERE b.wallet_id = w.id
  AND b.currency IS NULL;

-- CATEGORY_GLOBAL budgets have no wallet to infer currency from.
-- Defaulting to USD here is a placeholder, NOT a safe inference —
-- any existing CATEGORY_GLOBAL budget needs manual review by its
-- owner post-deploy to confirm the currency actually matches their
-- intent, since the system genuinely cannot know it.
UPDATE budgets
SET currency = 'USD'
WHERE currency IS NULL;

ALTER TABLE budgets
    ALTER COLUMN currency SET NOT NULL;