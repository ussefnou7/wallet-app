package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.profit.ProfitSummaryDto;
import com.wallet.walletapp.reporting.profit.ProfitSummaryReportServiceImpl;
import com.wallet.walletapp.transaction.ProfitSummaryProjection;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.user.Role;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfitSummaryReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @InjectMocks
    private ProfitSummaryReportServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateForOwnerUsesTenantScopedSummary() {
        authenticate(Role.OWNER);

        UUID walletId = UUID.randomUUID();
        LocalDateTime fromDate = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 1, 31, 23, 59);

        when(transactionRepository.getProfitSummary(TENANT_ID, walletId, fromDate, toDate))
                .thenReturn(projection(BigDecimal.TEN, BigDecimal.ONE));

        ProfitSummaryDto result = service.generate(fromDate, toDate, walletId);

        assertEquals(BigDecimal.TEN, result.getTotalWalletProfit());
        assertEquals(BigDecimal.ONE, result.getTotalCashProfit());
        assertEquals(BigDecimal.valueOf(11), result.getTotalProfit());
    }

    @Test
    void generateForUserWithoutWalletFilterUsesAssignedWalletsOnly() {
        authenticate(Role.USER);

        UUID walletId = UUID.randomUUID();
        UUID secondWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(walletId, secondWalletId));
        when(transactionRepository.getProfitSummaryForWallets(TENANT_ID, List.of(walletId, secondWalletId), null, null))
                .thenReturn(projection(BigDecimal.valueOf(5), BigDecimal.valueOf(2)));

        ProfitSummaryDto result = service.generate(null, null, null);

        assertEquals(BigDecimal.valueOf(5), result.getTotalWalletProfit());
        assertEquals(BigDecimal.valueOf(2), result.getTotalCashProfit());
        assertEquals(BigDecimal.valueOf(7), result.getTotalProfit());
    }

    @Test
    void generateForUserRejectsUnassignedWallet() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(null, null, unassignedWalletId));
        verifyNoInteractions(transactionRepository);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private ProfitSummaryProjection projection(BigDecimal totalWalletProfit, BigDecimal totalCashProfit) {
        return new ProfitSummaryProjection() {
            @Override
            public BigDecimal getTotalWalletProfit() {
                return totalWalletProfit;
            }

            @Override
            public BigDecimal getTotalCashProfit() {
                return totalCashProfit;
            }
        };
    }
}
