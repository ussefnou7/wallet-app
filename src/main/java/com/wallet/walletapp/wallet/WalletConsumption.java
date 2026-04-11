package com.wallet.walletapp.wallet;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Entity
@Table(name = "wallet_consumption")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletConsumption {

    @Id
    private Long walletId; // same as wallet id

    @MapsId
    @OneToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal dailyConsumed = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal monthlyConsumed = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate lastDailyReset;

    @Column(nullable = false)
    private YearMonth lastMonthlyReset;
}