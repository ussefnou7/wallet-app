package com.wallet.walletapp.wallet;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet extends TenantAwareEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String number;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletType type;

    @Column(nullable = false)
    private UUID branchId;

    // One-to-one with consumption
    @OneToOne(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WalletConsumption consumption;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyLimit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal monthlyLimit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal cashProfit = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal walletProfit = BigDecimal.ZERO;
}
