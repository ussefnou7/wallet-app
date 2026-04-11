package com.wallet.walletapp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ProfitReportResponse {

    private UUID tenantId;
    private BigDecimal totalProfit;
}
