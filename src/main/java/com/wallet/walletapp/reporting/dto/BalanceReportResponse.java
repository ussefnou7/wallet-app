package com.wallet.walletapp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class BalanceReportResponse {

    private UUID walletId;
    private String walletName;
    private BigDecimal balance;
}
