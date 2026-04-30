CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    recipient_user_id UUID NOT NULL,
    type VARCHAR(80) NOT NULL,
    priority VARCHAR(20) NOT NULL,
    priority_rank INT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title_key VARCHAR(150) NOT NULL,
    message_key VARCHAR(150) NOT NULL,
    payload_json JSONB,
    target_type VARCHAR(50),
    target_id UUID,
    idempotency_key VARCHAR(200),
    read_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_unread_priority_created
    ON notifications (recipient_user_id, read_at, priority_rank DESC, created_at DESC);

CREATE INDEX idx_notifications_read_at_not_null
    ON notifications (read_at)
    WHERE read_at IS NOT NULL;

CREATE UNIQUE INDEX ux_notifications_idempotency_key
    ON notifications (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
