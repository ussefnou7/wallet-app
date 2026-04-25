package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.ReportPeriod;
import com.wallet.walletapp.reporting.dto.TransactionTimeAggregationRowDto;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionTimeAggregationProjection;
import com.wallet.walletapp.user.Role;
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
public class TransactionTimeAggregationReportServiceImpl implements TransactionTimeAggregationReportService {

    private final TransactionRepository transactionRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<TransactionTimeAggregationRowDto> generate(@Nullable LocalDateTime fromDate,
                                                           @Nullable LocalDateTime toDate,
                                                           @Nullable UUID walletId,
                                                           @Nullable ReportPeriod period) {
        UserPrincipal user = currentUser();
        ReportPeriod resolvedPeriod = period != null ? period : ReportPeriod.DAILY;

        if (user.getRole() != Role.USER) {
            return mapRows(fetchTenantRows(user.getTenantId(), walletId, fromDate, toDate, resolvedPeriod));
        }

        List<UUID> assignedWalletIds = userWalletAccessService.getAccessibleWalletIds(user);

        if (walletId != null) {
            if (!assignedWalletIds.contains(walletId)) {
                throw new UnauthorizedException("Access denied to wallet");
            }
            return mapRows(fetchTenantRows(user.getTenantId(), walletId, fromDate, toDate, resolvedPeriod));
        }

        if (assignedWalletIds.isEmpty()) {
            return List.of();
        }

        return mapRows(fetchAssignedWalletRows(user.getTenantId(), assignedWalletIds, fromDate, toDate, resolvedPeriod));
    }

    private List<TransactionTimeAggregationProjection> fetchTenantRows(UUID tenantId,
                                                                       @Nullable UUID walletId,
                                                                       @Nullable LocalDateTime fromDate,
                                                                       @Nullable LocalDateTime toDate,
                                                                       ReportPeriod period) {
        return period == ReportPeriod.MONTHLY
                ? transactionRepository.getMonthlyTimeAggregation(tenantId, walletId, fromDate, toDate)
                : transactionRepository.getDailyTimeAggregation(tenantId, walletId, fromDate, toDate);
    }

    private List<TransactionTimeAggregationProjection> fetchAssignedWalletRows(UUID tenantId,
                                                                               List<UUID> walletIds,
                                                                               @Nullable LocalDateTime fromDate,
                                                                               @Nullable LocalDateTime toDate,
                                                                               ReportPeriod period) {
        return period == ReportPeriod.MONTHLY
                ? transactionRepository.getMonthlyTimeAggregationForWallets(tenantId, walletIds, fromDate, toDate)
                : transactionRepository.getDailyTimeAggregationForWallets(tenantId, walletIds, fromDate, toDate);
    }

    private List<TransactionTimeAggregationRowDto> mapRows(List<TransactionTimeAggregationProjection> rows) {
        return rows.stream()
                .map(this::toDto)
                .toList();
    }

    private TransactionTimeAggregationRowDto toDto(TransactionTimeAggregationProjection row) {
        BigDecimal totalCredits = row.getTotalCredits() != null ? row.getTotalCredits() : BigDecimal.ZERO;
        BigDecimal totalDebits = row.getTotalDebits() != null ? row.getTotalDebits() : BigDecimal.ZERO;
        Long transactionCount = row.getTransactionCount() != null ? row.getTransactionCount() : 0L;

        return new TransactionTimeAggregationRowDto(
                row.getPeriod(),
                totalCredits,
                totalDebits,
                totalCredits.subtract(totalDebits),
                transactionCount
        );
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
