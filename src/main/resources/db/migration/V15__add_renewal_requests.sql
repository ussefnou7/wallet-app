CREATE TABLE IF NOT EXISTS renewal_requests (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    requested_by UUID NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    amount NUMERIC(19,2) NOT NULL,
    period_months INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    reviewed_at TIMESTAMP NULL,
    reviewed_by UUID NULL,
    admin_note TEXT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_renewal_requests_tenant_id
    ON renewal_requests(tenant_id);

CREATE INDEX IF NOT EXISTS idx_renewal_requests_status
    ON renewal_requests(status);

CREATE INDEX IF NOT EXISTS idx_renewal_requests_created_at
    ON renewal_requests(created_at);
