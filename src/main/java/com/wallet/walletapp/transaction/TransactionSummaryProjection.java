package com.wallet.walletapp.transaction;

import java.math.BigDecimal;

public interface TransactionSummaryProjection {
    BigDecimal getTotalCredits();

    BigDecimal getTotalDebits();

    Long getTransactionCount();
}
