package com.wallet.walletapp.support;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SupportTicketReadProjection {

    UUID getId();

    UUID getTenantId();

    UUID getCreatedBy();

    String getSubject();

    String getDescription();

    SupportTicketType getType();

    SupportTicketPriority getPriority();

    SupportTicketStatus getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    LocalDateTime getResolvedAt();

    UUID getResolvedBy();

    String getCreatedByName();

    String getTenantName();
}
