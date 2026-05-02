package com.wallet.walletapp.reporting.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDto {
    private BigDecimal totalCredits;
    private BigDecimal totalDebits;
    private BigDecimal netAmount;
    private Long transactionCount;
}
