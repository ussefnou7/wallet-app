package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.TransactionReportReadModel;
import com.wallet.walletapp.transaction.TransactionType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionDetailsReportService {
    Page<TransactionReportReadModel> generate(@Nullable UUID walletId,
                                              @Nullable UUID branchId,
                                              @Nullable TransactionType type,
                                              @Nullable UUID createdByUserId,
                                              @Nullable Boolean cash,
                                              @Nullable LocalDateTime fromDate,
                                              @Nullable LocalDateTime toDate,
                                              int page,
                                              int size);
}
