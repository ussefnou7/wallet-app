package com.wallet.walletapp.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        if (!wallet.isActive()) {
            throw new IllegalStateException("Wallet is inactive");
        }

        if (request.getType() == TransactionType.DEBIT
                && wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient wallet balance");
        }

        Transaction transaction = new Transaction();
        transaction.setTenantId(tenantId);
        transaction.setWalletId(request.getWalletId());
        transaction.setPhoneNumber(request.getPhoneNumber());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setPercent(request.getPercent() != null ? request.getPercent() : java.math.BigDecimal.ZERO);
        transaction.setDescription(request.getDescription());

        if (request.getType() == TransactionType.CREDIT) {
            if (request.isCash())
                wallet.setCashProfit(wallet.getCashProfit().add(request.getAmount()));
            else
                wallet.setWalletProfit(wallet.getWalletProfit().add(request.getAmount()));
            wallet.setBalance(wallet.getBalance().add(request.getAmount()));
        } else {
            wallet.setCashProfit(wallet.getCashProfit().add(request.getAmount()));
            wallet.setBalance(wallet.getBalance().subtract(request.getAmount()));
        }

        walletRepository.save(wallet);
        transaction = transactionRepository.save(transaction);

        log.info("Transaction {} ({}) of {} created on wallet {}",
                transaction.getId(), transaction.getType(), transaction.getAmount(), wallet.getId());
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions(UUID walletId, TransactionType type,
                                                        LocalDateTime dateFrom, LocalDateTime dateTo) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        if (user.getRole() == Role.SYSTEM_ADMIN) {
            return transactionRepository.findAll().stream()
                    .map(transactionMapper::toResponse)
                    .collect(Collectors.toList());
        } else {
            return transactionRepository.findByFilters(tenantId, walletId, type, dateFrom, dateTo)
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
}
