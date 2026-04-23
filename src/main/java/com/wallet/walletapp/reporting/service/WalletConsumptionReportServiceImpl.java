package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.WalletConsumptionRowDto;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletConsumption;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.WalletUser;
import com.wallet.walletapp.wallet.WalletUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletConsumptionReportServiceImpl implements WalletConsumptionReportService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal NEAR_LIMIT_THRESHOLD = BigDecimal.valueOf(80);

    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;

    @Override
    @Transactional(readOnly = true)
    public List<WalletConsumptionRowDto> generate(@Nullable UUID walletId,
                                                  @Nullable UUID branchId,
                                                  @Nullable Boolean active) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();
        Set<UUID> assignedWalletIds = resolveAssignedWalletIds(user);

        if (user.getRole() == Role.USER && walletId != null && !assignedWalletIds.contains(walletId)) {
            throw new UnauthorizedException("Access denied to wallet");
        }

        if (user.getRole() == Role.USER && assignedWalletIds.isEmpty()) {
            return List.of();
        }

        return walletRepository.findAllByTenantIdOrderByIdAsc(tenantId).stream()
                .filter(wallet -> user.getRole() != Role.USER || assignedWalletIds.contains(wallet.getId()))
                .filter(wallet -> walletId == null || walletId.equals(wallet.getId()))
                .filter(wallet -> branchId == null || branchId.equals(wallet.getBranchId()))
                .filter(wallet -> active == null || active.equals(wallet.isActive()))
                .map(this::toDto)
                .toList();
    }

    private WalletConsumptionRowDto toDto(Wallet wallet) {
        WalletConsumption consumption = wallet.getConsumption();
        BigDecimal dailyConsumed = consumption != null && consumption.getDailyConsumed() != null
                ? consumption.getDailyConsumed()
                : BigDecimal.ZERO;
        BigDecimal monthlyConsumed = consumption != null && consumption.getMonthlyConsumed() != null
                ? consumption.getMonthlyConsumed()
                : BigDecimal.ZERO;
        BigDecimal dailyLimit = wallet.getDailyLimit() != null ? wallet.getDailyLimit() : BigDecimal.ZERO;
        BigDecimal monthlyLimit = wallet.getMonthlyLimit() != null ? wallet.getMonthlyLimit() : BigDecimal.ZERO;
        BigDecimal dailyUsagePercent = calculateUsagePercent(dailyConsumed, dailyLimit);
        BigDecimal monthlyUsagePercent = calculateUsagePercent(monthlyConsumed, monthlyLimit);

        WalletConsumptionRowDto dto = new WalletConsumptionRowDto();
        dto.setWalletId(wallet.getId());
        dto.setWalletName(wallet.getName());
        dto.setBranchId(wallet.getBranchId());
        dto.setActive(wallet.isActive());
        dto.setDailyConsumed(dailyConsumed);
        dto.setDailyLimit(dailyLimit);
        dto.setDailyUsagePercent(dailyUsagePercent);
        dto.setMonthlyConsumed(monthlyConsumed);
        dto.setMonthlyLimit(monthlyLimit);
        dto.setMonthlyUsagePercent(monthlyUsagePercent);
        dto.setNearDailyLimit(dailyUsagePercent.compareTo(NEAR_LIMIT_THRESHOLD) >= 0);
        dto.setNearMonthlyLimit(monthlyUsagePercent.compareTo(NEAR_LIMIT_THRESHOLD) >= 0);
        return dto;
    }

    private BigDecimal calculateUsagePercent(BigDecimal consumed, BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeConsumed = consumed != null ? consumed : BigDecimal.ZERO;
        return safeConsumed.multiply(ONE_HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);
    }

    private Set<UUID> resolveAssignedWalletIds(UserPrincipal user) {
        if (user.getRole() != Role.USER) {
            return Set.of();
        }

        return walletUserRepository.findByUserIdAndTenantId(user.getUserId(), user.getTenantId()).stream()
                .map(WalletUser::getWalletId)
                .collect(Collectors.toSet());
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
