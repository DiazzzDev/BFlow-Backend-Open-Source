-- Adds receipt-linking to expenses/incomes plus the ReceiptUpload
-- bridge table used by the "camera-first" OCR flow: user uploads a
-- photo, picks a wallet, Textract suggests a transaction, user
-- reviews and confirms before anything is persisted as an
-- Expense/Income.

-- Permanent link from a transaction to the receipt that generated
-- it (manual attach flow) or that Textract confirmed into it
-- (OCR flow) — both converge here.
ALTER TABLE expenses
    ADD COLUMN receipt_file_id UUID NULL
        REFERENCES stored_files (id) ON DELETE SET NULL;

ALTER TABLE incomes
    ADD COLUMN receipt_file_id UUID NULL
        REFERENCES stored_files (id) ON DELETE SET NULL;

CREATE INDEX idx_expenses_receipt_file_id
    ON expenses (receipt_file_id) WHERE receipt_file_id IS NOT NULL;

CREATE INDEX idx_incomes_receipt_file_id
    ON incomes (receipt_file_id) WHERE receipt_file_id IS NOT NULL;

-- Transitional state between "photo uploaded" and "transaction
-- exists": holds Textract's suggestion for review, then the
-- confirmed result once the user accepts it.
CREATE TABLE receipt_uploads (
                                 id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                 user_id                     UUID NOT NULL REFERENCES users (id),
                                 stored_file_id              UUID NOT NULL REFERENCES stored_files (id),
                                 wallet_id                   UUID NOT NULL REFERENCES wallets (id),
                                 status                      VARCHAR(20) NOT NULL DEFAULT 'RECEIVED',

    -- What Textract proposes. Nothing here is final until the user
    -- confirms; the frontend shows it as an editable draft.
                                 suggested_type              VARCHAR(10) NULL,   -- EXPENSE | INCOME
                                 suggested_title              VARCHAR(255) NULL,
                                 suggested_amount            NUMERIC(15,2) NULL,
                                 suggested_category_id       UUID NULL REFERENCES categories (id),
                                 suggested_date               DATE NULL,
                                 confidence_score            NUMERIC(5,2) NULL,
                                 raw_ocr_payload              JSONB NULL,

    -- Populated only once the user confirms.
                                 resulting_transaction_type   VARCHAR(10) NULL, -- EXPENSE | INCOME
                                 resulting_transaction_id     UUID NULL,

                                 failure_reason                TEXT NULL,
                                 created_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 updated_at                    TIMESTAMPTZ NOT NULL DEFAULT now(),
                                 CONSTRAINT uq_receipt_uploads_stored_file UNIQUE (stored_file_id)
);

CREATE INDEX idx_receipt_uploads_user_status
    ON receipt_uploads (user_id, status);