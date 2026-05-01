CREATE TABLE profit_collections
(
    id                   UUID PRIMARY KEY,
    tenant_id            UUID           NOT NULL,
    wallet_id            UUID           NOT NULL,
    branch_id            UUID,
    collected_by         UUID           NOT NULL,

    wallet_profit_amount NUMERIC(19, 2) NOT NULL DEFAULT 0,
    cash_profit_amount   NUMERIC(19, 2) NOT NULL DEFAULT 0,
    total_amount         NUMERIC(19, 2) NOT NULL DEFAULT 0,

    note                 TEXT,
    collected_at         TIMESTAMP      NOT NULL,
    created_at           TIMESTAMP      NOT NULL,
    updated_at           TIMESTAMP      NOT NULL,

    CONSTRAINT fk_profit_collection_wallet
        FOREIGN KEY (wallet_id) REFERENCES wallets (id)
);

CREATE INDEX idx_profit_collections_tenant_collected_at
    ON profit_collections (tenant_id, collected_at);

CREATE INDEX idx_profit_collections_wallet
    ON profit_collections (wallet_id);