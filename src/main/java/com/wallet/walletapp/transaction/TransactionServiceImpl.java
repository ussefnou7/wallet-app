package com.wallet.walletapp.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.common.TenantContext;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletConsumptionService;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final UserWalletAccessService userWalletAccessService;
    private final TransactionMapper transactionMapper;
    private final WalletConsumptionService walletConsumptionService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UserPrincipal user = currentUser();
        UUID tenantId = resolveTenantId(user);

        Wallet wallet = walletRepository.findByIdAndTenantId(request.getWalletId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found"));


        if (!wallet.isActive()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Wallet is inactive");
        }

        String externalTransactionId = normalizeExternalTransactionId(request.getExternalTransactionId());
        if (transactionRepository.findByTenantIdAndExternalTransactionId(tenantId, externalTransactionId).isPresent()) {
            throw new BusinessException(ErrorCode.DUPLICATED_TRANSACTION);
        }
        LocalDateTime occurredAt = resolveOccurredAt(request);

        Transaction transaction = buildTransaction(request, tenantId, externalTransactionId, occurredAt);
        transaction.setTenantId(tenantId);
        transaction.setCreatedBy(user.getUserId());

        Transaction saved = transactionRepository.saveAndFlush(transaction);

        applyBalanceUpdate(wallet, saved);
        walletRepository.save(wallet);
        walletConsumptionService.applyTransaction(wallet, saved);

        log.info("Transaction {} ({}) of {} created on wallet {}",
                saved.getId(), saved.getType(), saved.getAmount(), wallet.getId());

        return transactionMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionReadResponse> getAllTransactions(@Nullable UUID walletId, @Nullable TransactionType type,
                                                            @Nullable LocalDateTime dateFrom, @Nullable LocalDateTime dateTo) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() == Role.SYSTEM_ADMIN) {
            return transactionRepository.findAllForRead(walletId, type, dateFrom, dateTo).stream()
                    .map(transactionMapper::toReadResponse)
                    .collect(Collectors.toList());
        } else {
            return transactionRepository.findAllByTenantIdForRead(tenantId, walletId, type, dateFrom, dateTo).stream()
                    .map(transactionMapper::toReadResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionReadResponse getTransactionById(UUID id) {
        UserPrincipal user = currentUser();
        TransactionReadProjection transaction = transactionRepository.findReadByIdAndTenantId(id, user.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TRANSACTION_NOT_FOUND, "Transaction not found"));

        validateWalletAccess(user, transaction.getWalletId());
        return transactionMapper.toReadResponse(transaction);
    }

    private void validateWalletAccess(UserPrincipal user, UUID walletId) {
        if (user.getRole() == Role.USER && !userWalletAccessService.hasAccessToWallet(user, walletId)) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "Access denied to wallet", Map.of("walletId", walletId));
        }
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UUID resolveTenantId(UserPrincipal user) {
        UUID tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : user.getTenantId();
    }

    private String normalizeExternalTransactionId(String externalTransactionId) {
        if (externalTransactionId == null || externalTransactionId.isBlank()) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "External transaction id is required",
                    Map.of("externalTransactionId", "must not be blank")
            );
        }
        return externalTransactionId.trim();
    }

    private LocalDateTime resolveOccurredAt(CreateTransactionRequest request) {
        return request.getOccurredAt() != null
                ? request.getOccurredAt()
                : LocalDateTime.now();
    }

    private Transaction buildTransaction(CreateTransactionRequest request,
                                         UUID tenantId,
                                         String externalTransactionId,
                                         LocalDateTime occurredAt) {
        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setTenantId(tenantId);
        transaction.setExternalTransactionId(externalTransactionId);
        transaction.setOccurredAt(occurredAt);

        if (transaction.getPercent() == null) {
            transaction.setPercent(BigDecimal.ZERO);
        }

        return transaction;
    }

    private void applyBalanceUpdate(Wallet wallet, Transaction transaction) {
        if (transaction.getType() == TransactionType.CREDIT) {
            BigDecimal balance = wallet.getBalance().add(transaction.getAmount());
            if(transaction.isCash()) {
                wallet.setCashProfit(wallet.getCashProfit().add(transaction.getPercent()));
                balance = balance.add(transaction.getPercent());
            } else
                wallet.setWalletProfit(wallet.getWalletProfit().add(transaction.getPercent()));
            wallet.setBalance(balance);
        } else {
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
            wallet.setWalletProfit(wallet.getWalletProfit().add(transaction.getPercent()));
        }
    }
}
