package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.ProfitSummaryDto;
import com.wallet.walletapp.transaction.ProfitSummaryProjection;
import com.wallet.walletapp.transaction.TransactionRepository;
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
public class ProfitSummaryReportServiceImpl implements ProfitSummaryReportService {

    private final TransactionRepository transactionRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public ProfitSummaryDto generate(@Nullable LocalDateTime fromDate,
                                     @Nullable LocalDateTime toDate,
                                     @Nullable UUID walletId) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() != Role.USER) {
            return toDto(transactionRepository.getProfitSummary(tenantId, walletId, fromDate, toDate));
        }

        List<UUID> assignedWalletIds = userWalletAccessService.getAccessibleWalletIds(user);

        if (walletId != null) {
            if (!assignedWalletIds.contains(walletId)) {
                throw new UnauthorizedException("Access denied to wallet");
            }
            return toDto(transactionRepository.getProfitSummary(tenantId, walletId, fromDate, toDate));
        }

        if (assignedWalletIds.isEmpty()) {
            return zeroDto();
        }

        return toDto(transactionRepository.getProfitSummaryForWallets(tenantId, assignedWalletIds, fromDate, toDate));
    }

    private ProfitSummaryDto toDto(ProfitSummaryProjection result) {
        BigDecimal totalWalletProfit = result.getTotalWalletProfit();
        BigDecimal totalCashProfit = result.getTotalCashProfit();
        return new ProfitSummaryDto(
                totalWalletProfit,
                totalCashProfit,
                totalWalletProfit.add(totalCashProfit)
        );
    }

    private ProfitSummaryDto zeroDto() {
        return new ProfitSummaryDto(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
