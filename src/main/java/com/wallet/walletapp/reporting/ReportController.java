package com.wallet.walletapp.reporting;

import com.wallet.walletapp.reporting.dto.BalanceReportResponse;
import com.wallet.walletapp.reporting.dto.ProfitReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/balance")
    public ResponseEntity<BalanceReportResponse> getBalance(@RequestParam UUID walletId) {
        return ResponseEntity.ok(reportService.getBalance(walletId));
    }

    @GetMapping("/profit")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ProfitReportResponse> getProfit() {
        return ResponseEntity.ok(reportService.getProfit());
    }
}
