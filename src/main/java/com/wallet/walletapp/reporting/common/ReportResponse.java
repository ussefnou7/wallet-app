package com.wallet.walletapp.reporting.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse<T> {
    private String reportType;
    private String titleKey;
    private List<ReportColumnDto> columns;
    private T data;
    private ReportMetaResponse meta;

    public ReportResponse(String reportType, T data, ReportMetaResponse meta) {
        this.reportType = reportType;
        this.data = data;
        this.meta = meta;
    }
}
