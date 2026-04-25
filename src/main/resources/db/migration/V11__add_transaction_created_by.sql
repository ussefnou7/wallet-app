ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS created_by UUID;

CREATE INDEX IF NOT EXISTS idx_transactions_created_by
    ON transactions(created_by);
