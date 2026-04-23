package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.TransactionDetailRowDto;
import com.wallet.walletapp.transaction.Transaction;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionType;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletUser;
import com.wallet.walletapp.wallet.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionDetailsReportServiceImpl implements TransactionDetailsReportService {

    private static final int MAX_PAGE_SIZE = 100;

    private final TransactionRepository transactionRepository;
    private final WalletUserRepository walletUserRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionDetailRowDto> generate(@Nullable UUID walletId,
                                                  @Nullable TransactionType type,
                                                  @Nullable LocalDateTime fromDate,
                                                  @Nullable LocalDateTime toDate,
                                                  int page,
                                                  int size) {
        UserPrincipal user = currentUser();
        Pageable pageable = buildPageable(page, size);

        if (user.getRole() == Role.USER) {
            List<UUID> assignedWalletIds = walletUserRepository.findByUserIdAndTenantId(user.getUserId(), user.getTenantId())
                    .stream()
                    .map(WalletUser::getWalletId)
                    .toList();

            if (walletId != null) {
                if (!assignedWalletIds.contains(walletId)) {
                    throw new UnauthorizedException("Access denied to wallet");
                }
                return transactionRepository.findAllByFilters(user.getTenantId(), walletId, type, fromDate, toDate, pageable)
                        .map(this::toDto);
            }

            if (assignedWalletIds.isEmpty()) {
                return Page.empty(pageable);
            }

            return transactionRepository.findAllByFilters(user.getTenantId(), assignedWalletIds, type, fromDate, toDate, pageable)
                    .map(this::toDto);
        }

        return transactionRepository.findAllByFilters(user.getTenantId(), walletId, type, fromDate, toDate, pageable)
                .map(this::toDto);
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        return PageRequest.of(page, size);
    }

    private TransactionDetailRowDto toDto(Transaction transaction) {
        TransactionDetailRowDto dto = new TransactionDetailRowDto();
        dto.setId(transaction.getId());
        dto.setWalletId(transaction.getWalletId());
        dto.setAmount(transaction.getAmount());
        dto.setType(transaction.getType());
        dto.setPercent(transaction.getPercent());
        dto.setPhoneNumber(transaction.getPhoneNumber());
        dto.setCash(transaction.isCash());
        dto.setDescription(transaction.getDescription());
        dto.setOccurredAt(transaction.getOccurredAt());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
