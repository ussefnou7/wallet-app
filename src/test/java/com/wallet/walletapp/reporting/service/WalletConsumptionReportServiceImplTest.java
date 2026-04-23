package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletConsumption;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.WalletUser;
import com.wallet.walletapp.wallet.WalletUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletConsumptionReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletUserRepository walletUserRepository;

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
        UUID unassignedWalletId = UUID.randomUUID();
        Wallet assignedWallet = wallet(assignedWalletId, BigDecimal.ZERO, BigDecimal.ZERO, consumption(null, null));
        Wallet unassignedWallet = wallet(unassignedWalletId, BigDecimal.TEN, BigDecimal.TEN, consumption(BigDecimal.ONE, BigDecimal.ONE));

        when(walletUserRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(walletUser(assignedWalletId)));
        when(walletRepository.findAllByTenantIdOrderByIdAsc(TENANT_ID))
                .thenReturn(List.of(assignedWallet, unassignedWallet));

        var result = service.generate(null, null, null);

        assertEquals(1, result.size());
        assertEquals(assignedWalletId, result.getFirst().getWalletId());
        assertEquals(BigDecimal.ZERO, result.getFirst().getDailyConsumed());
        assertEquals(BigDecimal.ZERO, result.getFirst().getMonthlyConsumed());
        assertEquals(BigDecimal.ZERO, result.getFirst().getDailyUsagePercent());
        assertEquals(BigDecimal.ZERO, result.getFirst().getMonthlyUsagePercent());
        assertFalse(result.getFirst().getNearDailyLimit());
        assertFalse(result.getFirst().getNearMonthlyLimit());
    }

    @Test
    void generateForUserRejectsUnassignedWalletFilter() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(walletUserRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(walletUser(assignedWalletId)));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(unassignedWalletId, null, null));
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Wallet wallet(UUID walletId, BigDecimal dailyLimit, BigDecimal monthlyLimit, WalletConsumption consumption) {
        Wallet wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setTenantId(TENANT_ID);
        wallet.setName("wallet-" + walletId);
        wallet.setBranchId(UUID.randomUUID());
        wallet.setActive(true);
        wallet.setDailyLimit(dailyLimit);
        wallet.setMonthlyLimit(monthlyLimit);
        wallet.setConsumption(consumption);
        return wallet;
    }

    private WalletConsumption consumption(BigDecimal dailyConsumed, BigDecimal monthlyConsumed) {
        WalletConsumption consumption = new WalletConsumption();
        consumption.setDailyConsumed(dailyConsumed);
        consumption.setMonthlyConsumed(monthlyConsumed);
        return consumption;
    }

    private WalletUser walletUser(UUID walletId) {
        WalletUser walletUser = new WalletUser();
        walletUser.setUserId(USER_ID);
        walletUser.setWalletId(walletId);
        walletUser.setTenantId(TENANT_ID);
        return walletUser;
    }
}
