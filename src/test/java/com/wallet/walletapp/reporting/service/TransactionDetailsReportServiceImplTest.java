package com.wallet.walletapp.reporting.service;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.reporting.dto.TransactionDetailRowDto;
import com.wallet.walletapp.transaction.Transaction;
import com.wallet.walletapp.transaction.TransactionRepository;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletUser;
import com.wallet.walletapp.wallet.WalletUserRepository;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionDetailsReportServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletUserRepository walletUserRepository;

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
        Transaction transaction = transaction(assignedWalletId);

        when(walletUserRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(walletUser(assignedWalletId), walletUser(secondAssignedWalletId)));
        when(transactionRepository.findAllByFilters(
                TENANT_ID,
                List.of(assignedWalletId, secondAssignedWalletId),
                null,
                null,
                null,
                pageable))
                .thenReturn(new PageImpl<>(List.of(transaction), pageable, 1));

        Page<TransactionDetailRowDto> result = service.generate(null, null, null, null, 0, 20);

        assertEquals(1, result.getTotalElements());
        assertEquals(assignedWalletId, result.getContent().getFirst().getWalletId());
    }

    @Test
    void generateForUserRejectsUnassignedWalletFilter() {
        authenticate(Role.USER);

        UUID assignedWalletId = UUID.randomUUID();
        UUID unassignedWalletId = UUID.randomUUID();

        when(walletUserRepository.findByUserIdAndTenantId(USER_ID, TENANT_ID))
                .thenReturn(List.of(walletUser(assignedWalletId)));

        assertThrows(UnauthorizedException.class,
                () -> service.generate(unassignedWalletId, null, null, null, 0, 20));
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void generateRejectsUnboundedPageSize() {
        authenticate(Role.OWNER);

        assertThrows(IllegalArgumentException.class,
                () -> service.generate(null, null, null, null, 0, 101));
        verifyNoInteractions(transactionRepository, walletUserRepository);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private WalletUser walletUser(UUID walletId) {
        WalletUser walletUser = new WalletUser();
        walletUser.setUserId(USER_ID);
        walletUser.setWalletId(walletId);
        walletUser.setTenantId(TENANT_ID);
        return walletUser;
    }

    private Transaction transaction(UUID walletId) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setTenantId(TENANT_ID);
        transaction.setWalletId(walletId);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setOccurredAt(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }
}
