package com.wallet.walletapp.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.WalletConsumptionService;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserWalletAccessService userWalletAccessService;

    @Mock
    private WalletConsumptionService walletConsumptionService;

    private TransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(
                transactionRepository,
                walletRepository,
                userWalletAccessService,
                new TransactionMapper(),
                walletConsumptionService
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTransactionsReturnsDisplayFieldsFromProjection() {
        authenticate(Role.OWNER);

        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        when(transactionRepository.findAllByTenantIdForRead(TENANT_ID, null, null, null, null))
                .thenReturn(List.of(projection(transactionId, walletId, "Main Wallet", "owner-user")));

        List<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(walletId, result.getFirst().getWalletId());
        assertEquals("Main Wallet", result.getFirst().getWalletName());
        assertEquals("owner-user", result.getFirst().getCreatedByUsername());
        verify(transactionRepository).findAllByTenantIdForRead(TENANT_ID, null, null, null, null);
        verifyNoInteractions(walletRepository);
    }

    @Test
    void getTransactionByIdUsesTenantScopedReadProjection() {
        authenticate(Role.OWNER);

        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        when(transactionRepository.findReadByIdAndTenantId(transactionId, TENANT_ID))
                .thenReturn(Optional.of(projection(transactionId, walletId, "Retail Wallet", "branch-owner")));

        TransactionReadResponse result = service.getTransactionById(transactionId);

        assertEquals(transactionId, result.getId());
        assertEquals("Retail Wallet", result.getWalletName());
        assertEquals("branch-owner", result.getCreatedByUsername());
        verify(transactionRepository).findReadByIdAndTenantId(transactionId, TENANT_ID);
        verifyNoInteractions(walletRepository);
    }

    @Test
    void getTransactionByIdRejectsUserWithoutWalletAccess() {
        authenticate(Role.USER);

        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        when(transactionRepository.findReadByIdAndTenantId(transactionId, TENANT_ID))
                .thenReturn(Optional.of(projection(transactionId, walletId, "Restricted Wallet", "owner-user")));
        when(userWalletAccessService.hasAccessToWallet(org.mockito.ArgumentMatchers.any(UserPrincipal.class), org.mockito.ArgumentMatchers.eq(walletId)))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> service.getTransactionById(transactionId));
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "current-user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private TransactionReadProjection projection(UUID transactionId,
                                                 UUID walletId,
                                                 String walletName,
                                                 String createdByUsername) {
        return new TransactionReadProjection() {
            @Override
            public UUID getId() {
                return transactionId;
            }

            @Override
            public UUID getTenantId() {
                return TENANT_ID;
            }

            @Override
            public UUID getWalletId() {
                return walletId;
            }

            @Override
            public String getWalletName() {
                return walletName;
            }

            @Override
            public String getExternalTransactionId() {
                return "ext-" + transactionId;
            }

            @Override
            public BigDecimal getAmount() {
                return BigDecimal.TEN;
            }

            @Override
            public TransactionType getType() {
                return TransactionType.CREDIT;
            }

            @Override
            public BigDecimal getPercent() {
                return BigDecimal.ONE;
            }

            @Override
            public String getPhoneNumber() {
                return "0123456789";
            }

            @Override
            public Boolean getCash() {
                return Boolean.FALSE;
            }

            @Override
            public String getDescription() {
                return "test";
            }

            @Override
            public LocalDateTime getOccurredAt() {
                return LocalDateTime.of(2026, 4, 24, 10, 0);
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return LocalDateTime.of(2026, 4, 24, 10, 5);
            }

            @Override
            public LocalDateTime getUpdatedAt() {
                return LocalDateTime.of(2026, 4, 24, 10, 6);
            }

            @Override
            public UUID getCreatedBy() {
                return USER_ID;
            }

            @Override
            public String getCreatedByUsername() {
                return createdByUsername;
            }
        };
    }
}
