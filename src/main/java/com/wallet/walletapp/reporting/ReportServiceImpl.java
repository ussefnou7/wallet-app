package com.wallet.walletapp.reporting;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.BalanceReportResponse;
import com.wallet.walletapp.reporting.dto.ProfitReportResponse;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionType;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public BalanceReportResponse getBalance(UUID walletId) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        Wallet wallet = walletRepository.findByIdAndTenantId(walletId, tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found"));

        if (user.getRole() == Role.USER && !userWalletAccessService.hasAccessToWallet(user, walletId)) {
            throw new UnauthorizedException("Access denied to wallet");
        }

        BigDecimal credits = transactionRepository.sumAmountByWalletAndType(tenantId, walletId, TransactionType.CREDIT);
        BigDecimal debits  = transactionRepository.sumAmountByWalletAndType(tenantId, walletId, TransactionType.DEBIT);
        BigDecimal balance = credits.subtract(debits);

        return new BalanceReportResponse(wallet.getId(), wallet.getName(), balance);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfitReportResponse getProfit() {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();
        BigDecimal profit = transactionRepository.sumFeeByTenant(tenantId);
        return new ProfitReportResponse(tenantId, profit);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
