package com.wallet.walletapp.reporting.wallet;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@JsonPropertyOrder({
        "tenantName",
        "branchName",
        "walletName",
        "dailySpent",
        "monthlySpent",
        "yearlySpent",
        "dailyLimit",
        "monthlyLimit",
        "dailyPercent",
        "monthlyPercent",
        "updatedAt",
        "tenantId",
        "branchId",
        "walletId",
        "walletConsumptionId",
        "active",
        "nearDailyLimit",
        "nearMonthlyLimit"
})
public class WalletConsumptionReportReadModel {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal NEAR_LIMIT_THRESHOLD = BigDecimal.valueOf(80);

    private final UUID walletConsumptionId;
    private final UUID tenantId;
    private final String tenantName;
    private final UUID branchId;
    private final String branchName;
    private final UUID walletId;
    private final String walletName;
    private final BigDecimal dailySpent;
    private final BigDecimal monthlySpent;
    private final BigDecimal yearlySpent;
    private final BigDecimal dailyLimit;
    private final BigDecimal monthlyLimit;
    private final BigDecimal dailyPercent;
    private final BigDecimal monthlyPercent;
    private final LocalDateTime updatedAt;
    private final Boolean active;
    private final Boolean nearDailyLimit;
    private final Boolean nearMonthlyLimit;

    public WalletConsumptionReportReadModel(UUID walletConsumptionId,
                                            UUID tenantId,
                                            String tenantName,
                                            UUID branchId,
                                            String branchName,
                                            UUID walletId,
                                            String walletName,
                                            BigDecimal dailySpent,
                                            BigDecimal monthlySpent,
                                            BigDecimal dailyLimit,
                                            BigDecimal monthlyLimit,
                                            LocalDateTime updatedAt,
                                            Boolean active) {
        this.walletConsumptionId = walletConsumptionId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.branchId = branchId;
        this.branchName = branchName;
        this.walletId = walletId;
        this.walletName = walletName;
        this.dailySpent = defaultAmount(dailySpent);
        this.monthlySpent = defaultAmount(monthlySpent);
        this.yearlySpent = null;
        this.dailyLimit = defaultAmount(dailyLimit);
        this.monthlyLimit = defaultAmount(monthlyLimit);
        this.dailyPercent = calculateUsagePercent(this.dailySpent, this.dailyLimit);
        this.monthlyPercent = calculateUsagePercent(this.monthlySpent, this.monthlyLimit);
        // The wallet_consumption table does not currently store its own updated timestamp.
        this.updatedAt = updatedAt;
        this.active = active;
        this.nearDailyLimit = this.dailyPercent.compareTo(NEAR_LIMIT_THRESHOLD) >= 0;
        this.nearMonthlyLimit = this.monthlyPercent.compareTo(NEAR_LIMIT_THRESHOLD) >= 0;
    }

    private static BigDecimal defaultAmount(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal calculateUsagePercent(BigDecimal spent, BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return defaultAmount(spent).multiply(ONE_HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);
    }
}
