package com.wallet.walletapp.reporting;

import com.wallet.walletapp.reporting.common.*;
import com.wallet.walletapp.reporting.wallet.BalanceReportResponse;
import com.wallet.walletapp.reporting.dashboard.DashboardOverviewDto;
import com.wallet.walletapp.reporting.profit.ProfitReportResponse;
import com.wallet.walletapp.reporting.profit.ProfitSummaryDto;
import com.wallet.walletapp.reporting.transaction.TransactionReportReadModel;
import com.wallet.walletapp.reporting.transaction.TransactionSummaryDto;
import com.wallet.walletapp.reporting.dto.TransactionTimeAggregationRowDto;
import com.wallet.walletapp.reporting.wallet.WalletConsumptionReportReadModel;
import com.wallet.walletapp.reporting.dashboard.DashboardOverviewReportService;
import com.wallet.walletapp.reporting.profit.ProfitSummaryReportService;
import com.wallet.walletapp.reporting.transaction.TransactionDetailsReportService;
import com.wallet.walletapp.reporting.transaction.TransactionSummaryReportService;
import com.wallet.walletapp.reporting.service.TransactionTimeAggregationReportService;
import com.wallet.walletapp.reporting.wallet.WalletConsumptionReportService;
import com.wallet.walletapp.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final List<ReportColumnDto> TRANSACTION_SUMMARY_COLUMNS = List.of(
            new ReportColumnDto("totalCredits", "reports.fields.totalCredits", "money"),
            new ReportColumnDto("totalDebits", "reports.fields.totalDebits", "money"),
            new ReportColumnDto("netAmount", "reports.fields.netAmount", "money"),
            new ReportColumnDto("transactionCount", "reports.fields.transactionCount", "number")
    );
    private static final List<ReportColumnDto> PROFIT_SUMMARY_COLUMNS = List.of(
            new ReportColumnDto("totalWalletProfit", "reports.fields.totalWalletProfit", "money"),
            new ReportColumnDto("totalCashProfit", "reports.fields.totalCashProfit", "money"),
            new ReportColumnDto("totalProfit", "reports.fields.totalProfit", "money")
    );
    private static final List<ReportColumnDto> TRANSACTION_TIME_AGGREGATION_COLUMNS = List.of(
            new ReportColumnDto("period", "reports.fields.period", "text"),
            new ReportColumnDto("totalCredits", "reports.fields.totalCredits", "money"),
            new ReportColumnDto("totalDebits", "reports.fields.totalDebits", "money"),
            new ReportColumnDto("netAmount", "reports.fields.netAmount", "money"),
            new ReportColumnDto("transactionCount", "reports.fields.transactionCount", "number")
    );
    private static final List<ReportColumnDto> TRANSACTION_DETAILS_COLUMNS = List.of(
            new ReportColumnDto("tenantName", "reports.fields.tenantName", "text"),
            new ReportColumnDto("walletName", "reports.fields.walletName", "text"),
            new ReportColumnDto("createdByUsername", "reports.fields.createdByUsername", "text"),
            new ReportColumnDto("amount", "reports.fields.amount", "money"),
            new ReportColumnDto("type", "reports.fields.type", "text"),
            new ReportColumnDto("percent", "reports.fields.percent", "percent"),
            new ReportColumnDto("phoneNumber", "reports.fields.phoneNumber", "text"),
            new ReportColumnDto("cash", "reports.fields.cash", "boolean"),
            new ReportColumnDto("description", "reports.fields.description", "text"),
            new ReportColumnDto("occurredAt", "reports.fields.occurredAt", "datetime"),
            new ReportColumnDto("createdAt", "reports.fields.createdAt", "datetime"),
            new ReportColumnDto("transactionId", "reports.fields.transactionId", "text"),
            new ReportColumnDto("tenantId", "reports.fields.tenantId", "text"),
            new ReportColumnDto("walletId", "reports.fields.walletId", "text"),
            new ReportColumnDto("createdByUserId", "reports.fields.createdByUserId", "text")
    );
    private static final List<ReportColumnDto> WALLET_CONSUMPTION_COLUMNS = List.of(
            new ReportColumnDto("tenantName", "reports.fields.tenantName", "text"),
            new ReportColumnDto("branchName", "reports.fields.branchName", "text"),
            new ReportColumnDto("walletName", "reports.fields.walletName", "text"),
            new ReportColumnDto("dailySpent", "reports.fields.dailySpent", "money"),
            new ReportColumnDto("monthlySpent", "reports.fields.monthlySpent", "money"),
            new ReportColumnDto("yearlySpent", "reports.fields.yearlySpent", "money"),
            new ReportColumnDto("dailyLimit", "reports.fields.dailyLimit", "money"),
            new ReportColumnDto("monthlyLimit", "reports.fields.monthlyLimit", "money"),
            new ReportColumnDto("dailyPercent", "reports.fields.dailyPercent", "percent"),
            new ReportColumnDto("monthlyPercent", "reports.fields.monthlyPercent", "percent"),
            new ReportColumnDto("updatedAt", "reports.fields.updatedAt", "datetime"),
            new ReportColumnDto("tenantId", "reports.fields.tenantId", "text"),
            new ReportColumnDto("branchId", "reports.fields.branchId", "text"),
            new ReportColumnDto("walletId", "reports.fields.walletId", "text"),
            new ReportColumnDto("walletConsumptionId", "reports.fields.walletConsumptionId", "text"),
            new ReportColumnDto("active", "reports.fields.active", "boolean"),
            new ReportColumnDto("nearDailyLimit", "reports.fields.nearDailyLimit", "boolean"),
            new ReportColumnDto("nearMonthlyLimit", "reports.fields.nearMonthlyLimit", "boolean")
    );

    private final ReportService reportService;
    private final DashboardOverviewReportService dashboardOverviewReportService;
    private final ProfitSummaryReportService profitSummaryReportService;
    private final TransactionDetailsReportService transactionDetailsReportService;
    private final TransactionSummaryReportService transactionSummaryReportService;
    private final TransactionTimeAggregationReportService transactionTimeAggregationReportService;
    private final WalletConsumptionReportService walletConsumptionReportService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceReportResponse> getBalance(@RequestParam UUID walletId) {
        return ResponseEntity.ok(reportService.getBalance(walletId));
    }

    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ProfitReportResponse> getProfit() {
        return ResponseEntity.ok(reportService.getProfit());
    }

    @GetMapping("/dashboard/overview")
    public ResponseEntity<DashboardOverviewDto> getDashboardOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return ResponseEntity.ok(dashboardOverviewReportService.generate(fromDate, toDate));
    }

    @GetMapping("/profit/summary")
    public ResponseEntity<ReportResponse<ProfitSummaryDto>> getProfitSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) UUID walletId) {
        ProfitSummaryDto data = profitSummaryReportService.generate(fromDate, toDate, walletId);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<ProfitSummaryDto> response = new ReportResponse<>();
        response.setReportType(ReportType.PROFIT_SUMMARY.name());
        response.setTitleKey("reports.profitSummary.title");
        response.setColumns(PROFIT_SUMMARY_COLUMNS);
        response.setData(data);
        response.setMeta(meta);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/summary")
    public ResponseEntity<ReportResponse<TransactionSummaryDto>> getTransactionSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) UUID walletId) {
        TransactionSummaryDto data = transactionSummaryReportService.generate(fromDate, toDate, walletId);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<TransactionSummaryDto> response = new ReportResponse<>();
        response.setReportType(ReportType.TRANSACTION_SUMMARY.name());
        response.setTitleKey("reports.transactionSummary.title");
        response.setColumns(TRANSACTION_SUMMARY_COLUMNS);
        response.setData(data);
        response.setMeta(meta);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/time-aggregation")
    public ResponseEntity<ReportResponse<List<TransactionTimeAggregationRowDto>>> getTransactionTimeAggregation(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) UUID walletId,
            @RequestParam(defaultValue = "DAILY") ReportPeriod period) {
        List<TransactionTimeAggregationRowDto> data = transactionTimeAggregationReportService.generate(fromDate, toDate, walletId, period);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<List<TransactionTimeAggregationRowDto>> response = new ReportResponse<>();
        response.setReportType(ReportType.TRANSACTION_TIME_AGGREGATION.name());
        response.setTitleKey("reports.transactionTimeAggregation.title");
        response.setColumns(TRANSACTION_TIME_AGGREGATION_COLUMNS);
        response.setData(data);
        response.setMeta(meta);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/details")
    public ResponseEntity<ReportResponse<Page<TransactionReportReadModel>>> getTransactionDetails(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(name = "createdBy", required = false) UUID createdByUserId,
            @RequestParam(required = false) Boolean cash,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionReportReadModel> data = transactionDetailsReportService.generate(
                walletId,
                branchId,
                type,
                createdByUserId,
                cash,
                fromDate,
                toDate,
                page,
                size
        );
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<Page<TransactionReportReadModel>> response = new ReportResponse<>();
        response.setReportType(ReportType.TRANSACTION_DETAILS.name());
        response.setTitleKey("reports.transactionDetails.title");
        response.setColumns(TRANSACTION_DETAILS_COLUMNS);
        response.setData(data);
        response.setMeta(meta);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/wallets/consumption")
    public ResponseEntity<ReportResponse<List<WalletConsumptionReportReadModel>>> getWalletConsumption(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) Boolean active) {
        List<WalletConsumptionReportReadModel> data = walletConsumptionReportService.generate(walletId, branchId, active);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<List<WalletConsumptionReportReadModel>> response = new ReportResponse<>();
        response.setReportType(ReportType.WALLET_CONSUMPTION.name());
        response.setTitleKey("reports.walletConsumption.title");
        response.setColumns(WALLET_CONSUMPTION_COLUMNS);
        response.setData(data);
        response.setMeta(meta);
        return ResponseEntity.ok(response);
    }
}
