package com.wallet.walletapp.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.common.TenantContext;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletConsumptionService;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;
    private final TransactionMapper transactionMapper;
    private final WalletConsumptionService walletConsumptionService;

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UserPrincipal user = currentUser();
        UUID tenantId = resolveTenantId(user);

        Wallet wallet = walletRepository.findByIdAndTenantId(request.getWalletId(), tenantId)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));


        if (!wallet.isActive()) {
            throw new IllegalStateException("Wallet is inactive");
        }

        String externalTransactionId = normalizeExternalTransactionId(request.getExternalTransactionId());
        LocalDateTime occurredAt = resolveOccurredAt(request);

        Transaction transaction = buildTransaction(request, tenantId, externalTransactionId, occurredAt);
        transaction.setTenantId(tenantId);

        try {
            Transaction saved = transactionRepository.saveAndFlush(transaction);

            applyBalanceUpdate(wallet, saved);
            walletRepository.save(wallet);
            walletConsumptionService.applyTransaction(wallet, saved);

            log.info("Transaction {} ({}) of {} created on wallet {}",
                    saved.getId(), saved.getType(), saved.getAmount(), wallet.getId());

            return transactionMapper.toResponse(saved);

        } catch (DataIntegrityViolationException ex) {
            Transaction existing = transactionRepository
                    .findByTenantIdAndExternalTransactionId(tenantId, externalTransactionId)
                    .orElseThrow(() -> ex);

            return transactionMapper.toResponse(existing);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(@Nullable UUID walletId, @Nullable TransactionType type,
                                                        @Nullable LocalDateTime dateFrom, @Nullable LocalDateTime dateTo) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() == Role.SYSTEM_ADMIN) {
            return transactionRepository.findAll().stream()
                    .map(transactionMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return transactionRepository.findAll(
                            TransactionSpecifications.byFilters(tenantId, walletId, type, dateFrom, dateTo))
                    .stream()
                    .map(transactionMapper::toResponse)
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(UUID id) {
        UserPrincipal user = currentUser();
        Transaction transaction = transactionRepository.findByIdAndTenantId(id, user.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        validateWalletAccess(user, transaction.getWalletId(), user.getTenantId());
        return transactionMapper.toResponse(transaction);
    }

    private void validateWalletAccess(UserPrincipal user, UUID walletId, UUID tenantId) {
        if (user.getRole() == Role.USER) {
            boolean hasAccess = walletUserRepository.existsByUserIdAndWalletIdAndTenantId(
                    user.getUserId(), walletId, tenantId);
            if (!hasAccess) {
                throw new UnauthorizedException("Access denied to wallet");
            }
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
            throw new IllegalArgumentException("External transaction id is required");
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
            wallet.setBalance(wallet.getBalance().add(transaction.getAmount()));
            if(transaction.isCash())
                wallet.setCashProfit(wallet.getCashProfit().add(transaction.getAmount()));
            else
                wallet.setWalletProfit(wallet.getWalletProfit().add(transaction.getAmount()));
        } else
            wallet.setBalance(wallet.getBalance().subtract(transaction.getAmount()));
    }
}
