package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.WalletConsumptionReportReadModel;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletConsumptionRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletConsumptionReportServiceImpl implements WalletConsumptionReportService {

    private final WalletConsumptionRepository walletConsumptionRepository;
    private final UserWalletAccessService userWalletAccessService;

    @Override
    @Transactional(readOnly = true)
    public List<WalletConsumptionReportReadModel> generate(@Nullable UUID walletId,
                                                           @Nullable UUID branchId,
                                                           @Nullable Boolean active) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();
        Set<UUID> assignedWalletIds = resolveAssignedWalletIds(user);

        if (user.getRole() == Role.USER && walletId != null && !assignedWalletIds.contains(walletId)) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "Access denied to wallet", Map.of("walletId", walletId));
        }

        if (user.getRole() == Role.USER && assignedWalletIds.isEmpty()) {
            return List.of();
        }

        if (user.getRole() == Role.USER) {
            return walletConsumptionRepository.findReportByTenantIdAndWalletIdIn(
                    tenantId,
                    assignedWalletIds,
                    walletId,
                    branchId,
                    active
            );
        }

        return walletConsumptionRepository.findReportByTenantId(tenantId, walletId, branchId, active);
    }

    private Set<UUID> resolveAssignedWalletIds(UserPrincipal user) {
        if (user.getRole() != Role.USER) {
            return Set.of();
        }

        return Set.copyOf(userWalletAccessService.getAccessibleWalletIds(user));
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
