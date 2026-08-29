-- Adds the Textract JobId to receipt_uploads so the SNS completion
-- notification (which only carries JobId, not our receiptId) can be
-- correlated back to the ReceiptUpload that requested it.

ALTER TABLE receipt_uploads
    ADD COLUMN textract_job_id VARCHAR(255) NULL;

-- Looked up once per SNS notification (low volume, but unique so a
-- duplicate/replayed notification can't silently match two rows).
CREATE UNIQUE INDEX idx_receipt_uploads_textract_job_id
    ON receipt_uploads (textract_job_id) WHERE textract_job_id IS NOT NULL;
