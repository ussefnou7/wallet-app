package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.TransactionSummaryDto;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionSummaryReportServiceImpl implements TransactionSummaryReportService {

    private final TransactionRepository transactionRepository;
    private final WalletUserRepository walletUserRepository;

    @Transactional(readOnly = true)
    public TransactionSummaryDto generate(@Nullable LocalDateTime fromDate,
                                          @Nullable LocalDateTime toDate,
                                          @Nullable UUID walletId) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() == Role.USER && walletId == null) {
            throw new BusinessValidationException("USER role must specify walletId");
        }

        if (user.getRole() == Role.USER && walletId != null) {
            boolean hasAccess = walletUserRepository.existsByUserIdAndWalletIdAndTenantId(
                    user.getUserId(), walletId, tenantId);
            if (!hasAccess) {
                throw new UnauthorizedException("Access denied to wallet");
            }
        }

        Object[] result = transactionRepository.getSummary(tenantId, walletId, fromDate, toDate);

        BigDecimal totalCredits = (BigDecimal) result[0];
        BigDecimal totalDebits = (BigDecimal) result[1];
        Long count = (Long) result[2];

        BigDecimal netAmount = totalCredits.subtract(totalDebits);

        return new TransactionSummaryDto(totalCredits, totalDebits, netAmount, count);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
