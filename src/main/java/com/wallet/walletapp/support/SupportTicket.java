package com.wallet.walletapp.support;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_support_tickets_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_support_tickets_status", columnList = "status"),
        @Index(name = "idx_support_tickets_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID createdBy;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupportTicketPriority priority = SupportTicketPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SupportTicketStatus status = SupportTicketStatus.OPEN;

    private LocalDateTime resolvedAt;

    private UUID resolvedBy;
}
