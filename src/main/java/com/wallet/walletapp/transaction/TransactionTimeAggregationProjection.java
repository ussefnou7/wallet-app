package com.wallet.walletapp.transaction;

import java.math.BigDecimal;

public interface TransactionTimeAggregationProjection {
    String getPeriod();

    BigDecimal getTotalCredits();

    BigDecimal getTotalDebits();

    Long getTransactionCount();
}
