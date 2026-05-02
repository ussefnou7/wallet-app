package com.wallet.walletapp.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface WalletReadProjection {

    UUID getId();

    UUID getTenantId();

    String getTenantName();

    UUID getBranchId();

    String getBranchName();

    String getName();

    String getNumber();

    BigDecimal getBalance();

    BigDecimal getWalletProfit();

    BigDecimal getCashProfit();

    BigDecimal getDailyLimit();

    BigDecimal getMonthlyLimit();

    boolean getActive();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    WalletType getType();

    LocalDateTime getCollectedAt();

    String getCollectedByName();
}
