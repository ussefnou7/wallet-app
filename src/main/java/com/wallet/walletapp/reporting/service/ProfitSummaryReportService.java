package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.ProfitSummaryDto;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProfitSummaryReportService {
    ProfitSummaryDto generate(@Nullable LocalDateTime fromDate,
                              @Nullable LocalDateTime toDate,
                              @Nullable UUID walletId);
}
