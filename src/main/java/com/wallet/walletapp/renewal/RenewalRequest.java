package com.wallet.walletapp.renewal;

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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "renewal_requests", indexes = {
        @Index(name = "idx_renewal_requests_tenant_id", columnList = "tenant_id"),
        @Index(name = "idx_renewal_requests_status", columnList = "status"),
        @Index(name = "idx_renewal_requests_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenewalRequest extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID requestedBy;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private Integer periodMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RenewalRequestStatus status = RenewalRequestStatus.PENDING;

    private LocalDateTime reviewedAt;

    private UUID reviewedBy;

    @Column(columnDefinition = "text")
    private String adminNote;
}
