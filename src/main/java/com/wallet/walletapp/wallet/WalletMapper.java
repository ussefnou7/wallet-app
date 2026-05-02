package com.wallet.walletapp.wallet;

import com.wallet.walletapp.wallet.dto.WalletResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setTenantId(wallet.getTenantId());
        response.setBranchId(wallet.getBranchId());
        response.setName(wallet.getName());
        response.setNumber(wallet.getNumber());
        response.setBalance(wallet.getBalance());
        response.setCashProfit(wallet.getCashProfit());
        response.setWalletProfit(wallet.getWalletProfit());
        response.setDailyLimit(wallet.getDailyLimit());
        response.setMonthlyLimit(wallet.getMonthlyLimit());
        WalletConsumption consumption = wallet.getConsumption();
        BigDecimal dailySpent = consumption != null ? consumption.getDailyConsumed() : BigDecimal.ZERO;
        BigDecimal monthlySpent = consumption != null ? consumption.getMonthlyConsumed() : BigDecimal.ZERO;
        response.setDailySpent(dailySpent);
        response.setMonthlySpent(monthlySpent);
        response.setDailyPercent(toPercent(dailySpent, wallet.getDailyLimit()));
        response.setMonthlyPercent(toPercent(monthlySpent, wallet.getMonthlyLimit()));
        response.setType(wallet.getType());
        response.setActive(wallet.isActive());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }

    public WalletResponse toResponse(WalletReadProjection projection, WalletConsumption consumption) {
        WalletResponse response = new WalletResponse();
        response.setId(projection.getId());
        response.setTenantId(projection.getTenantId());
        response.setTenantName(projection.getTenantName());
        response.setBranchId(projection.getBranchId());
        response.setBranchName(projection.getBranchName());
        response.setName(projection.getName());
        response.setNumber(projection.getNumber());
        response.setBalance(projection.getBalance());
        response.setCashProfit(projection.getCashProfit());
        response.setWalletProfit(projection.getWalletProfit());
        response.setDailyLimit(projection.getDailyLimit());
        response.setMonthlyLimit(projection.getMonthlyLimit());
        BigDecimal dailySpent = consumption != null ? consumption.getDailyConsumed() : BigDecimal.ZERO;
        BigDecimal monthlySpent = consumption != null ? consumption.getMonthlyConsumed() : BigDecimal.ZERO;
        response.setDailySpent(dailySpent);
        response.setMonthlySpent(monthlySpent);
        response.setDailyPercent(toPercent(dailySpent, projection.getDailyLimit()));
        response.setMonthlyPercent(toPercent(monthlySpent, projection.getMonthlyLimit()));
        response.setType(projection.getType());
        response.setCollectedAt(projection.getCollectedAt());
        response.setCollectedByName(projection.getCollectedByName());
        response.setActive(projection.getActive());
        response.setCreatedAt(projection.getCreatedAt());
        response.setUpdatedAt(projection.getUpdatedAt());
        return response;
    }

    private BigDecimal toPercent(BigDecimal spent, BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return spent.multiply(BigDecimal.valueOf(100))
                .divide(limit, 2, RoundingMode.HALF_UP);
    }
}
