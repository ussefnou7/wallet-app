package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.DashboardOverviewDto;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public interface DashboardOverviewReportService {
    DashboardOverviewDto generate(@Nullable LocalDateTime fromDate,
                                  @Nullable LocalDateTime toDate);
}
