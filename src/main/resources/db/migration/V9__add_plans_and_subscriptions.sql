CREATE TABLE plans (
    id UUID PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    max_users INTEGER NOT NULL,
    max_wallets INTEGER NOT NULL,
    max_branches INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE tenant_subscriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL UNIQUE REFERENCES tenants(id) ON DELETE CASCADE,
    plan_id UUID NOT NULL REFERENCES plans(id),
    start_date DATE NOT NULL,
    expire_date DATE NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
