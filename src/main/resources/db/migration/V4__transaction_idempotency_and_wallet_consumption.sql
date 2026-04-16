ALTER TABLE wallets
    ADD COLUMN IF NOT EXISTS number VARCHAR(255),
    ADD COLUMN IF NOT EXISTS type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS branch_id UUID,
    ADD COLUMN IF NOT EXISTS daily_limit DECIMAL(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS monthly_limit DECIMAL(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS cash_profit DECIMAL(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS wallet_profit DECIMAL(19, 2) NOT NULL DEFAULT 0;

UPDATE wallets
SET number = COALESCE(number, id::text),
    type = COALESCE(type, 'Vodafone'),
    branch_id = COALESCE(branch_id, gen_random_uuid()),
    daily_limit = COALESCE(daily_limit, 0),
    monthly_limit = COALESCE(monthly_limit, 0),
    cash_profit = COALESCE(cash_profit, 0),
    wallet_profit = COALESCE(wallet_profit, 0)
WHERE number IS NULL
   OR type IS NULL
   OR branch_id IS NULL
   OR daily_limit IS NULL
   OR monthly_limit IS NULL
   OR cash_profit IS NULL
   OR wallet_profit IS NULL;

ALTER TABLE wallets
    ALTER COLUMN number SET NOT NULL,
    ALTER COLUMN type SET NOT NULL,
    ALTER COLUMN branch_id SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'transactions'
          AND column_name = 'fee'
    ) AND NOT EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'transactions'
          AND column_name = 'percent'
    ) THEN
        ALTER TABLE transactions RENAME COLUMN fee TO percent;
    END IF;
END $$;

ALTER TABLE transactions
    ADD COLUMN IF NOT EXISTS external_transaction_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS percent DECIMAL(19, 2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(50),
    ADD COLUMN IF NOT EXISTS is_cash BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS occurred_at TIMESTAMP;

UPDATE transactions
SET external_transaction_id = COALESCE(external_transaction_id, id::text),
    percent = COALESCE(percent, 0),
    phone_number = COALESCE(phone_number, ''),
    occurred_at = COALESCE(occurred_at, created_at, NOW())
WHERE external_transaction_id IS NULL
   OR percent IS NULL
   OR phone_number IS NULL
   OR occurred_at IS NULL;

ALTER TABLE transactions
    ALTER COLUMN external_transaction_id SET NOT NULL,
    ALTER COLUMN phone_number SET NOT NULL,
    ALTER COLUMN occurred_at SET NOT NULL;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS expiry_date TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_transactions_tenant_external_transaction'
    ) THEN
        ALTER TABLE transactions
            ADD CONSTRAINT uk_transactions_tenant_external_transaction
            UNIQUE (tenant_id, external_transaction_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_transactions_tenant_external_transaction
    ON transactions (tenant_id, external_transaction_id);

DROP TABLE IF EXISTS wallet_consumption;

CREATE TABLE wallet_consumption (
    wallet_id UUID PRIMARY KEY REFERENCES wallets(id) ON DELETE CASCADE,
    daily_consumed DECIMAL(19, 2) NOT NULL DEFAULT 0,
    monthly_consumed DECIMAL(19, 2) NOT NULL DEFAULT 0,
    daily_window_date DATE NOT NULL,
    monthly_window_key VARCHAR(7) NOT NULL,
    last_processed_transaction_id UUID,
    last_processed_occurred_at TIMESTAMP
);

INSERT INTO wallet_consumption (
    wallet_id,
    daily_consumed,
    monthly_consumed,
    daily_window_date,
    monthly_window_key,
    last_processed_transaction_id,
    last_processed_occurred_at
)
SELECT
    w.id,
    COALESCE(daily_totals.total_amount, 0),
    COALESCE(monthly_totals.total_amount, 0),
    CURRENT_DATE,
    TO_CHAR(CURRENT_DATE, 'YYYY-MM'),
    latest_tx.id,
    latest_tx.occurred_at
FROM wallets w
LEFT JOIN LATERAL (
    SELECT SUM(t.amount) AS total_amount
    FROM transactions t
    WHERE t.wallet_id = w.id
      AND t.type = 'DEBIT'
      AND t.occurred_at >= CURRENT_DATE::timestamp
      AND t.occurred_at < (CURRENT_DATE + INTERVAL '1 day')::timestamp
) daily_totals ON TRUE
LEFT JOIN LATERAL (
    SELECT SUM(t.amount) AS total_amount
    FROM transactions t
    WHERE t.wallet_id = w.id
      AND t.type = 'DEBIT'
      AND t.occurred_at >= date_trunc('month', CURRENT_DATE)::timestamp
      AND t.occurred_at < (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')::timestamp
) monthly_totals ON TRUE
LEFT JOIN LATERAL (
    SELECT t.id, t.occurred_at
    FROM transactions t
    WHERE t.wallet_id = w.id
    ORDER BY t.occurred_at DESC, t.created_at DESC, t.id DESC
    LIMIT 1
) latest_tx ON TRUE;

INSERT INTO public.users (
    created_at,
    updated_at,
    id,
    tenant_id,
    password,
    role,
    username,
    active,
    expiry_date
)
SELECT
    TIMESTAMP '2026-04-11 02:52:29.363',
    TIMESTAMP '2026-04-11 02:52:29.363',
    'b2d08175-f0c0-4bf3-bcca-672d5965f7e4'::uuid,
    '171c2ec8-09c9-4c74-b14c-ad0c5bd48030'::uuid,
    '$2a$10$JmOCvE7zb16E5wc8jH/S3ORUGYF7UhEg4yYnJs9G7UaA23OLHfA0u',
    'SYSTEM_ADMIN',
    'Ussef',
    TRUE,
    NULL
WHERE NOT EXISTS (
    SELECT 1
    FROM public.users u
    WHERE u.id = 'b2d08175-f0c0-4bf3-bcca-672d5965f7e4'::uuid
       OR u.username = 'Ussef'
);
