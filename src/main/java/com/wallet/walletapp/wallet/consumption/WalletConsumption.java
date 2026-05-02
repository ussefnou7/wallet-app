package com.wallet.walletapp.wallet.consumption;

import com.wallet.walletapp.wallet.Wallet;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_consumption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletConsumption implements Persistable<UUID> {

    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal dailyConsumed = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal monthlyConsumed = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate dailyWindowDate;

    @Column(nullable = false, length = 7)
    private String monthlyWindowKey;

    private UUID lastProcessedTransactionId;

    private LocalDateTime lastProcessedOccurredAt;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public UUID getId() {
        return walletId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
