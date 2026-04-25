package com.wallet.walletapp.wallet;

import java.math.BigDecimal;

public interface DashboardWalletMetricsProjection {
    BigDecimal getTotalBalance();

    Long getActiveWallets();
}
