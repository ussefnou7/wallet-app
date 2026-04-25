package com.wallet.walletapp.transaction;

import java.math.BigDecimal;

public interface ProfitSummaryProjection {
    BigDecimal getTotalWalletProfit();

    BigDecimal getTotalCashProfit();
}
