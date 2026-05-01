package com.wallet.walletapp.wallet.profit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProfitCollectionResponse(
        UUID id,
        UUID walletId,
        UUID branchId,
        BigDecimal walletProfitAmount,
        BigDecimal cashProfitAmount,
        BigDecimal totalAmount,
        BigDecimal remainingWalletProfit,
        BigDecimal remainingCashProfit,
        String note,
        LocalDateTime collectedAt
) {}