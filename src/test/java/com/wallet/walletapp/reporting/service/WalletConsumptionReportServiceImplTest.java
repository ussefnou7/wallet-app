package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.WalletConsumptionReportReadModel;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletConsumptionRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletConsumptionReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private WalletConsumptionRepository walletConsumptionRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @InjectMocks
    private WalletConsumptionReportServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateForUserOnlyReturnsAssignedWalletsAndSafelyHandlesNullConsumption() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        WalletConsumptionReportReadModel assignedWallet = readModel(
                assignedWalletId,
                branchId,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId));
        when(walletConsumptionRepository.findReportByTenantIdAndWalletIdIn(
                TENANT_ID,
                Set.of(assignedWalletId),
                null,
                null,
                null))
                .thenReturn(List.of(assignedWallet));

        var result = service.generate(null, null, null);

        assertEquals(1, result.size());
        assertEquals(assignedWalletId, result.getFirst().getWalletId());
        assertEquals("Branch A", result.getFirst().getBranchName());
        assertEquals(BigDecimal.ZERO, result.getFirst().getDailySpent());
        assertEquals(BigDecimal.ZERO, result.getFirst().getMonthlySpent());
        assertEquals(BigDecimal.ZERO, result.getFirst().getDailyPercent());
        assertEquals(BigDecimal.ZERO, result.getFirst().getMonthlyPercent());
        assertFalse(result.getFirst().getNearDailyLimit());
        assertFalse(result.getFirst().getNearMonthlyLimit());
    }

    @Test
    void generateForUserRejectsUnassignedWalletFilter() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(unassignedWalletId, null, null));
        verifyNoInteractions(walletConsumptionRepository);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private WalletConsumptionReportReadModel readModel(UUID walletId,
                                                       UUID branchId,
                                                       BigDecimal dailySpent,
                                                       BigDecimal monthlySpent,
                                                       BigDecimal dailyLimit,
                                                       BigDecimal monthlyLimit) {
        return new WalletConsumptionReportReadModel(
                walletId,
                TENANT_ID,
                "Tenant A",
                branchId,
                "Branch A",
                walletId,
                "wallet-" + walletId,
                dailySpent,
                monthlySpent,
                dailyLimit,
                monthlyLimit,
                LocalDateTime.of(2026, 4, 24, 9, 0),
                Boolean.TRUE
        );
    }
}
