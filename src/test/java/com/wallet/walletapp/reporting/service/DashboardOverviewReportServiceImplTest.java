package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.reporting.dashboard.DashboardOverviewDto;
import com.wallet.walletapp.reporting.dashboard.DashboardOverviewReportServiceImpl;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionSummaryProjection;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.DashboardWalletMetricsProjection;
import com.wallet.walletapp.wallet.WalletRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardOverviewReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @InjectMocks
    private DashboardOverviewReportServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateForOwnerUsesTenantWideMetrics() {
        authenticate(Role.OWNER);

        LocalDateTime fromDate = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 4, 30, 23, 59);

        when(walletRepository.getActiveDashboardMetricsByTenantId(TENANT_ID))
                .thenReturn(walletMetrics(BigDecimal.valueOf(1250), 3L));
        when(transactionRepository.getSummary(TENANT_ID, null, fromDate, toDate))
                .thenReturn(transactionMetrics(BigDecimal.valueOf(400), BigDecimal.valueOf(150), 7L));

        DashboardOverviewDto result = service.generate(fromDate, toDate);

        assertEquals(BigDecimal.valueOf(1250), result.getTotalBalance());
        assertEquals(3L, result.getActiveWallets());
        assertEquals(BigDecimal.valueOf(400), result.getTotalCredits());
        assertEquals(BigDecimal.valueOf(150), result.getTotalDebits());
        assertEquals(BigDecimal.valueOf(250), result.getNetAmount());
        assertEquals(7L, result.getTransactionCount());
    }

    @Test
    void generateForUserUsesAssignedWalletsOnly() {
        authenticate(Role.USER);

        UUID walletId = UUID.randomUUID();
        UUID secondWalletId = UUID.randomUUID();
        List<UUID> assignedWalletIds = List.of(walletId, secondWalletId);

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(assignedWalletIds);
        when(walletRepository.getActiveDashboardMetricsByTenantIdAndWalletIdIn(TENANT_ID, assignedWalletIds))
                .thenReturn(walletMetrics(BigDecimal.valueOf(800), 2L));
        when(transactionRepository.getSummaryForWallets(TENANT_ID, assignedWalletIds, null, null))
                .thenReturn(transactionMetrics(BigDecimal.valueOf(250), BigDecimal.valueOf(100), 5L));

        DashboardOverviewDto result = service.generate(null, null);

        assertEquals(BigDecimal.valueOf(800), result.getTotalBalance());
        assertEquals(2L, result.getActiveWallets());
        assertEquals(BigDecimal.valueOf(250), result.getTotalCredits());
        assertEquals(BigDecimal.valueOf(100), result.getTotalDebits());
        assertEquals(BigDecimal.valueOf(150), result.getNetAmount());
        assertEquals(5L, result.getTransactionCount());
    }

    @Test
    void generateForUserWithoutAssignedWalletsReturnsZeroOverview() {
        authenticate(Role.USER);

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of());

        DashboardOverviewDto result = service.generate(null, null);

        assertEquals(BigDecimal.ZERO, result.getTotalBalance());
        assertEquals(0L, result.getActiveWallets());
        assertEquals(BigDecimal.ZERO, result.getTotalCredits());
        assertEquals(BigDecimal.ZERO, result.getTotalDebits());
        assertEquals(BigDecimal.ZERO, result.getNetAmount());
        assertEquals(0L, result.getTransactionCount());
        verifyNoInteractions(walletRepository, transactionRepository);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private DashboardWalletMetricsProjection walletMetrics(BigDecimal totalBalance, Long activeWallets) {
        return new DashboardWalletMetricsProjection() {
            @Override
            public BigDecimal getTotalBalance() {
                return totalBalance;
            }

            @Override
            public Long getActiveWallets() {
                return activeWallets;
            }
        };
    }

    private TransactionSummaryProjection transactionMetrics(BigDecimal totalCredits,
                                                            BigDecimal totalDebits,
                                                            Long transactionCount) {
        return new TransactionSummaryProjection() {
            @Override
            public BigDecimal getTotalCredits() {
                return totalCredits;
            }

            @Override
            public BigDecimal getTotalDebits() {
                return totalDebits;
            }

            @Override
            public Long getTransactionCount() {
                return transactionCount;
            }
        };
    }
}
