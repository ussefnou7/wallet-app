package com.wallet.walletapp.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse<T> {
    private String reportType;
    private T data;
    private ReportMetaResponse meta;
}
