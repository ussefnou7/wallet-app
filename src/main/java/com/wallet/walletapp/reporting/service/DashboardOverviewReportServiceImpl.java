package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.reporting.dto.DashboardOverviewDto;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionSummaryProjection;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.DashboardWalletMetricsProjection;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardOverviewReportServiceImpl implements DashboardOverviewReportService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewDto generate(@Nullable LocalDateTime fromDate,
                                         @Nullable LocalDateTime toDate) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        DashboardWalletMetricsProjection walletMetrics;
        TransactionSummaryProjection transactionMetrics;

        if (user.getRole() == Role.USER) {
            List<UUID> assignedWalletIds = userWalletAccessService.getAccessibleWalletIds(user);

            if (assignedWalletIds.isEmpty()) {
                return emptyOverview();
            }

            walletMetrics = walletRepository.getActiveDashboardMetricsByTenantIdAndWalletIdIn(tenantId, assignedWalletIds);
            transactionMetrics = transactionRepository.getSummaryForWallets(tenantId, assignedWalletIds, fromDate, toDate);
        } else {
            walletMetrics = walletRepository.getActiveDashboardMetricsByTenantId(tenantId);
            transactionMetrics = transactionRepository.getSummary(tenantId, null, fromDate, toDate);
        }

        BigDecimal totalBalance = walletMetrics != null ? zeroIfNull(walletMetrics.getTotalBalance()) : BigDecimal.ZERO;
        Long activeWallets = walletMetrics != null ? zeroIfNull(walletMetrics.getActiveWallets()) : 0L;
        BigDecimal totalCredits = transactionMetrics != null ? zeroIfNull(transactionMetrics.getTotalCredits()) : BigDecimal.ZERO;
        BigDecimal totalDebits = transactionMetrics != null ? zeroIfNull(transactionMetrics.getTotalDebits()) : BigDecimal.ZERO;
        Long transactionCount = transactionMetrics != null ? zeroIfNull(transactionMetrics.getTransactionCount()) : 0L;

        return new DashboardOverviewDto(
                totalBalance,
                activeWallets,
                totalCredits,
                totalDebits,
                totalCredits.subtract(totalDebits),
                transactionCount
        );
    }

    private DashboardOverviewDto emptyOverview() {
        return new DashboardOverviewDto(
                BigDecimal.ZERO,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                0L
        );
    }

    private BigDecimal zeroIfNull(@Nullable BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private Long zeroIfNull(@Nullable Long value) {
        return value != null ? value : 0L;
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
