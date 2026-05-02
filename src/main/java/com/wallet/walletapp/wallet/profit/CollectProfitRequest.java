package com.wallet.walletapp.wallet.profit;

import java.math.BigDecimal;

public record CollectProfitRequest(
        BigDecimal walletProfitAmount,
        BigDecimal cashProfitAmount,
        String note
) {}