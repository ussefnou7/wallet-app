package com.wallet.walletapp.wallet.profit.dto;

import java.math.BigDecimal;

public record CollectProfitRequest(
        BigDecimal walletProfitAmount,
        BigDecimal cashProfitAmount,
        String note
) {}