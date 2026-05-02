package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.common.ReportPeriod;
import com.wallet.walletapp.reporting.dto.TransactionTimeAggregationRowDto;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionTimeAggregationReportService {
    List<TransactionTimeAggregationRowDto> generate(@Nullable LocalDateTime fromDate,
                                                    @Nullable LocalDateTime toDate,
                                                    @Nullable UUID walletId,
                                                    @Nullable ReportPeriod period);
}
