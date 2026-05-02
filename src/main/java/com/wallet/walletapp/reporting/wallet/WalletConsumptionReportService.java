package com.wallet.walletapp.reporting.wallet;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface WalletConsumptionReportService {
    List<WalletConsumptionReportReadModel> generate(@Nullable UUID walletId,
                                                    @Nullable UUID branchId,
                                                    @Nullable Boolean active);
}
