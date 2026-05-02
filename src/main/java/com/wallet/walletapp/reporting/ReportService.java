package com.wallet.walletapp.reporting;

import com.wallet.walletapp.reporting.wallet.BalanceReportResponse;
import com.wallet.walletapp.reporting.profit.ProfitReportResponse;

import java.util.UUID;

public interface ReportService {

    BalanceReportResponse getBalance(UUID walletId);

    ProfitReportResponse getProfit();
}
