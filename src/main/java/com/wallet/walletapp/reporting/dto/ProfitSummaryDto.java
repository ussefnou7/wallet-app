package com.wallet.walletapp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ProfitSummaryDto {
    private BigDecimal totalWalletProfit;
    private BigDecimal totalCashProfit;
    private BigDecimal totalProfit;
}
