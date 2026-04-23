package com.wallet.walletapp.reporting;

import com.wallet.walletapp.reporting.dto.BalanceReportResponse;
import com.wallet.walletapp.reporting.dto.ProfitReportResponse;
import com.wallet.walletapp.reporting.dto.ReportMetaResponse;
import com.wallet.walletapp.reporting.dto.ReportResponse;
import com.wallet.walletapp.reporting.dto.TransactionDetailRowDto;
import com.wallet.walletapp.reporting.dto.TransactionSummaryDto;
import com.wallet.walletapp.reporting.dto.WalletConsumptionRowDto;
import com.wallet.walletapp.reporting.service.TransactionDetailsReportService;
import com.wallet.walletapp.reporting.service.TransactionSummaryReportService;
import com.wallet.walletapp.reporting.service.WalletConsumptionReportService;
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

    private final ReportService reportService;
    private final TransactionDetailsReportService transactionDetailsReportService;
    private final TransactionSummaryReportService transactionSummaryReportService;
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

    @GetMapping("/transactions/summary")
    public ResponseEntity<ReportResponse<TransactionSummaryDto>> getTransactionSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) UUID walletId) {
        TransactionSummaryDto data = transactionSummaryReportService.generate(fromDate, toDate, walletId);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<TransactionSummaryDto> response = new ReportResponse<>(
                ReportType.TRANSACTION_SUMMARY.name(),
                data,
                meta
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/transactions/details")
    public ResponseEntity<Page<TransactionDetailRowDto>> getTransactionDetails(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionDetailsReportService.generate(walletId, type, fromDate, toDate, page, size));
    }

    @GetMapping("/wallets/consumption")
    public ResponseEntity<ReportResponse<List<WalletConsumptionRowDto>>> getWalletConsumption(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) UUID branchId,
            @RequestParam(required = false) Boolean active) {
        List<WalletConsumptionRowDto> data = walletConsumptionReportService.generate(walletId, branchId, active);
        ReportMetaResponse meta = new ReportMetaResponse(LocalDateTime.now());
        ReportResponse<List<WalletConsumptionRowDto>> response = new ReportResponse<>(
                ReportType.WALLET_CONSUMPTION.name(),
                data,
                meta
        );
        return ResponseEntity.ok(response);
    }
}
