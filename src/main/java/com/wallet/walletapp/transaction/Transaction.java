package com.wallet.walletapp.transaction;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID walletId;

    @Column(nullable = false, length = 255)
    private String externalTransactionId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal percent = BigDecimal.ZERO;

    @Column(nullable = false, length = 50)
    private String phoneNumber;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCash = false;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime occurredAt;   
}
