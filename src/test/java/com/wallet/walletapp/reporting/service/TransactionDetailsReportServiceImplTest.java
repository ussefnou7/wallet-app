package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.TransactionReportReadModel;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.transaction.TransactionType;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDetailsReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @InjectMocks
    private TransactionDetailsReportServiceImpl service;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void generateForUserWithoutWalletFilterOnlyUsesAssignedWallets() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID secondAssignedWalletId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 20);
        TransactionReportReadModel transaction = transaction(assignedWalletId, "Main Wallet", "owner-user");

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId, secondAssignedWalletId));
        when(transactionRepository.findTransactionReportByTenantIdAndWalletIdIn(
                TENANT_ID,
                List.of(assignedWalletId, secondAssignedWalletId),
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                pageable))
                .thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        Page<TransactionReportReadModel> result = service.generate(null, null, null, null, null, null, null, 0, 20);

        assertEquals(1, result.getNumberOfElements());
        assertEquals(assignedWalletId, result.getContent().getFirst().getWalletId());
        assertEquals("Main Wallet", result.getContent().getFirst().getWalletName());
    }

    @Test
    void generateForUserRejectsUnassignedWalletFilter() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(userWalletAccessService.getAccessibleWalletIds(org.mockito.ArgumentMatchers.any(UserPrincipal.class)))
                .thenReturn(List.of(assignedWalletId));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(unassignedWalletId, null, null, null, null, null, null, 0, 20));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void generateRejectsUnboundedPageSize() {
        authenticate(Role.OWNER);

        assertThrows(IllegalArgumentException.class,
                () -> service.generate(null, null, null, null, null, null, null, 0, 101));
        verifyNoInteractions(transactionRepository, userWalletAccessService);
    }

    @Test
    void generateForOwnerPassesAllSupportedFiltersToRepository() {
        authenticate(Role.OWNER);

        UUID walletId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID createdByUserId = UUID.randomUUID();
        LocalDateTime fromDate = LocalDateTime.of(2026, 4, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 4, 30, 23, 59);
        PageRequest pageable = PageRequest.of(1, 10);
        TransactionReportReadModel transaction = transaction(walletId, "Retail Wallet", "branch-owner");

        when(transactionRepository.findTransactionReportByTenantId(
                TENANT_ID,
                walletId,
                branchId,
                TransactionType.DEBIT,
                true,
                createdByUserId,
                Boolean.TRUE,
                fromDate,
                toDate,
                pageable))
                .thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        Page<TransactionReportReadModel> result = service.generate(
                walletId,
                branchId,
                TransactionType.DEBIT,
                createdByUserId,
                Boolean.TRUE,
                fromDate,
                toDate,
                1,
                10
        );

        assertEquals(1, result.getNumberOfElements());
        assertEquals("branch-owner", result.getContent().getFirst().getCreatedByUsername());
        verify(transactionRepository).findTransactionReportByTenantId(
                TENANT_ID,
                walletId,
                branchId,
                TransactionType.DEBIT,
                true,
                createdByUserId,
                Boolean.TRUE,
                fromDate,
                toDate,
                pageable
        );
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private TransactionReportReadModel transaction(UUID walletId, String walletName, String createdByUsername) {
        return new TransactionReportReadModel(
                UUID.randomUUID(),
                TENANT_ID,
                "Tenant A",
                walletId,
                walletName,
                USER_ID,
                createdByUsername,
                BigDecimal.TEN,
                TransactionType.CREDIT,
                BigDecimal.ONE,
                "0123456789",
                Boolean.FALSE,
                "test",
                LocalDateTime.of(2026, 4, 24, 10, 0),
                LocalDateTime.of(2026, 4, 24, 10, 5)
        );
    }
}
