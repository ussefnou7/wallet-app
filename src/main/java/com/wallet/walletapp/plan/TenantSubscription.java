package com.wallet.walletapp.plan;

import com.wallet.walletapp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tenant_subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantSubscription extends BaseEntity {

    @Column(nullable = false, unique = true)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID planId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate expireDate;
}
