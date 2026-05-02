package com.wallet.walletapp.reporting.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionSummaryProjection;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionSummaryReportServiceImpl implements TransactionSummaryReportService {

    private final TransactionRepository transactionRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Transactional(readOnly = true)
    public TransactionSummaryDto generate(@Nullable LocalDateTime fromDate,
                                          @Nullable LocalDateTime toDate,
                                          @Nullable UUID walletId) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() == Role.USER && walletId == null) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "USER role must specify walletId",
                    Map.of("walletId", "must not be null")
            );
        }

        if (user.getRole() == Role.USER && walletId != null) {
            if (!userWalletAccessService.hasAccessToWallet(user, walletId)) {
                throw new UnauthorizedException("Access denied to wallet");
            }
        }

        TransactionSummaryProjection result = transactionRepository.getSummary(tenantId, walletId, fromDate, toDate);

        BigDecimal totalCredits = result.getTotalCredits();
        BigDecimal totalDebits = result.getTotalDebits();
        Long count = result.getTransactionCount();

        BigDecimal netAmount = totalCredits.subtract(totalDebits);

        return new TransactionSummaryDto(totalCredits, totalDebits, netAmount, count);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
