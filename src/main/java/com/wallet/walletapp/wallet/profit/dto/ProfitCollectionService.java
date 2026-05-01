package com.wallet.walletapp.wallet.profit.dto;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.profit.ProfitCollection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfitCollectionService {

    private final  ProfitCollectionRepository profitCollectionRepository;
    private final WalletRepository walletRepository;
    @Transactional
    public ProfitCollectionResponse collectProfit(UUID walletId, CollectProfitRequest request) {
        UserPrincipal authContext = currentUser();
        UUID tenantId = authContext.getTenantId();
        UUID userId = authContext.getUserId();

        Wallet wallet = walletRepository.findByIdAndTenantIdForUpdate(walletId, tenantId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        BigDecimal walletAmount = safe(request.walletProfitAmount());
        BigDecimal cashAmount = safe(request.cashProfitAmount());

        if (walletAmount.compareTo(BigDecimal.ZERO) < 0 || cashAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }

        if (walletAmount.compareTo(BigDecimal.ZERO) == 0 && cashAmount.compareTo(BigDecimal.ZERO) == 0) {
            throw new BusinessException(ErrorCode.INVALID_AMOUNT);
        }

        if (walletAmount.compareTo(wallet.getWalletProfit()) > 0) {
            throw new BusinessException(ErrorCode.WALLET_PROFIT_COLLECTION_EXCEEDS_AVAILABLE);
        }

        if (cashAmount.compareTo(wallet.getCashProfit()) > 0) {
            throw new BusinessException(ErrorCode.CASH_PROFIT_COLLECTION_EXCEEDS_AVAILABLE);
        }

        wallet.setWalletProfit(wallet.getWalletProfit().subtract(walletAmount));
        wallet.setCashProfit(wallet.getCashProfit().subtract(cashAmount));

        ProfitCollection collection = ProfitCollection.builder()
                .walletId(wallet.getId())
                .branchId(wallet.getBranchId())
                .collectedBy(userId)
                .walletProfitAmount(walletAmount)
                .cashProfitAmount(cashAmount)
                .totalAmount(walletAmount.add(cashAmount))
                .note(request.note())
                .collectedAt(LocalDateTime.now())
                .build();

        collection.setTenantId(tenantId);

        profitCollectionRepository.save(collection);
        walletRepository.save(wallet);

        return new ProfitCollectionResponse(
                collection.getId(),
                wallet.getId(),
                wallet.getBranchId(),
                walletAmount,
                cashAmount,
                collection.getTotalAmount(),
                wallet.getWalletProfit(),
                wallet.getCashProfit(),
                collection.getNote(),
                collection.getCollectedAt()
        );
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
