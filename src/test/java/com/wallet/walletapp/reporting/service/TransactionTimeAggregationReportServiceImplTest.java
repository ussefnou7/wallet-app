package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.ReportPeriod;
import com.wallet.walletapp.reporting.dto.TransactionTimeAggregationRowDto;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionTimeAggregationProjection;
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
class TransactionTimeAggregationReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @InjectMocks
    private TransactionTimeAggregationReportServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateDailyAggregationMapsRows() {
        authenticate(Role.OWNER);
        UUID walletId = UUID.randomUUID();
        LocalDateTime fromDate = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 4, 30, 23, 59);

        when(transactionRepository.getDailyTimeAggregation(TENANT_ID, walletId, fromDate, toDate))
                .thenReturn(List.of(projection("2026-04-01", BigDecimal.TEN, BigDecimal.ONE, 2L)));

        List<TransactionTimeAggregationRowDto> result = service.generate(fromDate, toDate, walletId, ReportPeriod.DAILY);

        assertEquals(1, result.size());
        assertEquals("2026-04-01", result.getFirst().getPeriod());
        assertEquals(BigDecimal.TEN, result.getFirst().getTotalCredits());
        assertEquals(BigDecimal.ONE, result.getFirst().getTotalDebits());
        assertEquals(BigDecimal.valueOf(9), result.getFirst().getNetAmount());
        assertEquals(2L, result.getFirst().getTransactionCount());
    }

    @Test
    void generateMonthlyAggregationUsesMonthlyQuery() {
        authenticate(Role.OWNER);

        when(transactionRepository.getMonthlyTimeAggregation(TENANT_ID, null, null, null))
                .thenReturn(List.of(projection("2026-04", BigDecimal.valueOf(50), BigDecimal.valueOf(15), 5L)));

        List<TransactionTimeAggregationRowDto> result = service.generate(null, null, null, ReportPeriod.MONTHLY);

        assertEquals(1, result.size());
        assertEquals("2026-04", result.getFirst().getPeriod());
        assertEquals(BigDecimal.valueOf(35), result.getFirst().getNetAmount());
    }

    @Test
    void generateForUserUsesAssignedWalletsOnly() {
        authenticate(Role.USER);
        UUID walletId = UUID.randomUUID();
        UUID secondWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(walletId, secondWalletId));
        when(transactionRepository.getDailyTimeAggregationForWallets(TENANT_ID, List.of(walletId, secondWalletId), null, null))
                .thenReturn(List.of(projection("2026-04-01", BigDecimal.ONE, BigDecimal.ZERO, 1L)));

        List<TransactionTimeAggregationRowDto> result = service.generate(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("2026-04-01", result.getFirst().getPeriod());
        assertEquals(BigDecimal.ONE, result.getFirst().getTotalCredits());
        assertEquals(BigDecimal.ZERO, result.getFirst().getTotalDebits());
    }

    @Test
    void generateForUserRejectsUnassignedWallet() {
        authenticate(Role.USER);
        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(null, null, unassignedWalletId, ReportPeriod.DAILY));
        verifyNoInteractions(transactionRepository);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private TransactionTimeAggregationProjection projection(String period,
                                                            BigDecimal totalCredits,
                                                            BigDecimal totalDebits,
                                                            Long transactionCount) {
        return new TransactionTimeAggregationProjection() {
            @Override
            public String getPeriod() {
                return period;
            }

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
