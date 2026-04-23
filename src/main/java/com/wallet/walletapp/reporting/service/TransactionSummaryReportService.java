package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.TransactionSummaryDto;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionSummaryReportService {
    TransactionSummaryDto generate(@Nullable LocalDateTime fromDate,
                                   @Nullable LocalDateTime toDate,
                                   @Nullable UUID walletId);
}
