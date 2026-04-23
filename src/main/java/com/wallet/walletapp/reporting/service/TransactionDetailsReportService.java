package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.TransactionDetailRowDto;
import com.wallet.walletapp.transaction.TransactionType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionDetailsReportService {
    Page<TransactionDetailRowDto> generate(@Nullable UUID walletId,
                                           @Nullable TransactionType type,
                                           @Nullable LocalDateTime fromDate,
                                           @Nullable LocalDateTime toDate,
                                           int page,
                                           int size);
}
