package com.wallet.walletapp.reporting.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportColumnDto {
    private String key;
    private String labelKey;
    private String type;
}
