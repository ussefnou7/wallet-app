package com.wallet.walletapp.wallet.profit;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "profit_collections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfitCollection extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID walletId;

    private UUID branchId;

    @Column(nullable = false)
    private UUID collectedBy;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal walletProfitAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal cashProfitAmount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    private String note;

    @Column(nullable = false)
    private LocalDateTime collectedAt;
}