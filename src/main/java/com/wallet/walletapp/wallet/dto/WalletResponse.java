package com.wallet.walletapp.wallet.dto;

import com.wallet.walletapp.wallet.WalletType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WalletResponse {

    private UUID id;
    private UUID tenantId;
    private String tenantName;
    private UUID branchId;
    private String name;
    private String number;
    private BigDecimal balance;
    private BigDecimal walletProfit;
    private BigDecimal cashProfit;
    private BigDecimal dailyLimit;
    private BigDecimal monthlyLimit;
    private BigDecimal dailySpent;
    private BigDecimal monthlySpent;
    private BigDecimal dailyPercent;
    private BigDecimal monthlyPercent;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private WalletType type;
}
