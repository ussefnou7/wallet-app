package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.TransactionReportReadModel;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionType;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.UserWalletAccessService;
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
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionReportReadModel> generate(@Nullable UUID walletId,
                                                     @Nullable UUID branchId,
                                                     @Nullable TransactionType type,
                                                     @Nullable UUID createdByUserId,
                                                     @Nullable Boolean cash,
                                                     @Nullable LocalDateTime fromDate,
                                                     @Nullable LocalDateTime toDate,
                                                     int page,
                                                     int size) {
        UserPrincipal user = currentUser();
        Pageable pageable = buildPageable(page, size);
        boolean filterByCreatedBy = createdByUserId != null;

        if (user.getRole() == Role.USER) {
            List<UUID> assignedWalletIds = userWalletAccessService.getAccessibleWalletIds(user);

            if (walletId != null && !assignedWalletIds.contains(walletId)) {
                throw new UnauthorizedException("Access denied to wallet");
            }

            if (assignedWalletIds.isEmpty()) {
                return Page.empty(pageable);
            }

            return transactionRepository.findTransactionReportByTenantIdAndWalletIdIn(
                    user.getTenantId(),
                    assignedWalletIds,
                    walletId,
                    branchId,
                    type,
                    filterByCreatedBy,
                    createdByUserId,
                    cash,
                    fromDate,
                    toDate,
                    pageable
            );
        }

        return transactionRepository.findTransactionReportByTenantId(
                user.getTenantId(),
                walletId,
                branchId,
                type,
                filterByCreatedBy,
                createdByUserId,
                cash,
                fromDate,
                toDate,
                pageable
        );
    }

    private Pageable buildPageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        return PageRequest.of(page, size);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
