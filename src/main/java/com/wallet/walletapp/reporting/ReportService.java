package com.wallet.walletapp.reporting;

import com.wallet.walletapp.reporting.dto.BalanceReportResponse;
import com.wallet.walletapp.reporting.dto.ProfitReportResponse;

import java.util.UUID;

public interface ReportService {

    BalanceReportResponse getBalance(UUID walletId);

    ProfitReportResponse getProfit();
}
