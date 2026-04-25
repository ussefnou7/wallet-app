CREATE TABLE IF NOT EXISTS attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    uploaded_by UUID NOT NULL,
    module_type VARCHAR(50) NOT NULL,
    reference_id UUID NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    size_bytes BIGINT NOT NULL,
    storage_provider VARCHAR(50) NOT NULL,
    storage_key VARCHAR(255) NOT NULL,
    url VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_attachments_storage_key UNIQUE (storage_key)
);

CREATE INDEX IF NOT EXISTS idx_attachments_module_reference
    ON attachments(module_type, reference_id);

CREATE INDEX IF NOT EXISTS idx_attachments_tenant_id
    ON attachments(tenant_id);
