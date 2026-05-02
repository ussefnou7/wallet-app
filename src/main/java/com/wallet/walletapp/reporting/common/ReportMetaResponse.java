package com.wallet.walletapp.reporting.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetaResponse {
    private LocalDateTime generatedAt;
}
