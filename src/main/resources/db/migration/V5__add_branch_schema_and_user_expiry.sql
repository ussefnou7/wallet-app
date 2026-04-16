ALTER TABLE users
    ADD COLUMN IF NOT EXISTS expiry_date TIMESTAMP;

CREATE TABLE IF NOT EXISTS branches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_branches_tenant_id ON branches(tenant_id);

CREATE TABLE IF NOT EXISTS branch_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    user_id UUID NOT NULL,
    branch_id UUID NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_branch_users_user_branch UNIQUE (user_id, branch_id)
);

CREATE INDEX IF NOT EXISTS idx_branch_users_tenant_id ON branch_users(tenant_id);
CREATE INDEX IF NOT EXISTS idx_branch_users_user_id ON branch_users(user_id);
CREATE INDEX IF NOT EXISTS idx_branch_users_branch_id ON branch_users(branch_id);
