package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.reporting.dto.WalletConsumptionRowDto;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface WalletConsumptionReportService {
    List<WalletConsumptionRowDto> generate(@Nullable UUID walletId,
                                           @Nullable UUID branchId,
                                           @Nullable Boolean active);
}
