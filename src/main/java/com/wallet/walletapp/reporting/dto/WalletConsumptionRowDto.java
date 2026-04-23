package com.wallet.walletapp.reporting.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class WalletConsumptionRowDto {
    private UUID walletId;
    private String walletName;
    private UUID branchId;
    private Boolean active;
    private BigDecimal dailyConsumed;
    private BigDecimal dailyLimit;
    private BigDecimal dailyUsagePercent;
    private BigDecimal monthlyConsumed;
    private BigDecimal monthlyLimit;
    private BigDecimal monthlyUsagePercent;
    private Boolean nearDailyLimit;
    private Boolean nearMonthlyLimit;
}
