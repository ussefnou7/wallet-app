package com.wallet.walletapp.transaction;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.common.dto.PageResponse;
import com.wallet.walletapp.notification.TransactionNotificationService;
import com.wallet.walletapp.notification.WalletConsumptionNotificationService;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletConsumption;
import com.wallet.walletapp.wallet.WalletConsumptionService;
import com.wallet.walletapp.wallet.WalletRepository;
import com.wallet.walletapp.wallet.WalletType;
import com.wallet.walletapp.wallet.UserWalletAccessService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
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

    @Mock
    private TransactionNotificationService transactionNotificationService;

    @Mock
    private WalletConsumptionNotificationService walletConsumptionNotificationService;

    private TransactionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TransactionServiceImpl(
                transactionRepository,
                walletRepository,
                userWalletAccessService,
                new TransactionMapper(),
                walletConsumptionService,
                transactionNotificationService,
                walletConsumptionNotificationService
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllTransactionsReturnsFirstPageWithDefaultSize() {
        authenticate(Role.OWNER);

        UUID transactionId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        Page<TransactionReadProjection> transactionPage = new PageImpl<>(
                List.of(projection(transactionId, walletId, "Main Wallet", "owner-user")),
                Pageable.ofSize(20),
                1
        );
        when(transactionRepository.findAllByTenantIdForRead(eq(TENANT_ID), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(transactionPage);

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null, 0, 20);

        assertEquals(1, result.content().size());
        assertEquals(walletId, result.content().getFirst().getWalletId());
        assertEquals("Main Wallet", result.content().getFirst().getWalletName());
        assertEquals("owner-user", result.content().getFirst().getCreatedByUsername());
        assertEquals(0, result.page());
        assertEquals(20, result.size());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertTrue(result.last());
        assertFalse(result.hasNext());
        verify(transactionRepository).findAllByTenantIdForRead(
                eq(TENANT_ID),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 20)
        );
        verifyNoInteractions(walletRepository);
    }

    @Test
    void getAllTransactionsCapsSizeAt100() {
        authenticate(Role.OWNER);

        when(transactionRepository.findAllByTenantIdForRead(eq(TENANT_ID), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(Page.empty(Pageable.ofSize(100)));

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null, 0, 500);

        assertEquals(100, result.size());
        verify(transactionRepository).findAllByTenantIdForRead(
                eq(TENANT_ID),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 100)
        );
    }

    @Test
    void getAllTransactionsTreatsNegativePageAsZero() {
        authenticate(Role.OWNER);

        when(transactionRepository.findAllByTenantIdForRead(eq(TENANT_ID), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(Page.empty(Pageable.ofSize(10)));

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null, -3, 10);

        assertEquals(0, result.page());
        assertEquals(10, result.size());
        verify(transactionRepository).findAllByTenantIdForRead(
                eq(TENANT_ID),
                eq(null),
                eq(null),
                eq(null),
                eq(null),
                argThat(pageable -> pageable.getPageNumber() == 0 && pageable.getPageSize() == 10)
        );
    }

    @Test
    void getAllTransactionsUsesAccessibleWalletsForUser() {
        authenticate(Role.USER);

        UUID walletId = UUID.randomUUID();
        when(userWalletAccessService.getAccessibleWalletIds(any(UserPrincipal.class))).thenReturn(List.of(walletId));
        when(transactionRepository.findAllByTenantIdAndWalletIdInForRead(
                eq(TENANT_ID), eq(List.of(walletId)), eq(null), eq(null), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(
                        List.of(projection(UUID.randomUUID(), walletId, "Assigned Wallet", "owner-user")),
                        Pageable.ofSize(20),
                        1
                ));

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null, 0, 20);

        assertEquals(1, result.content().size());
        assertEquals(walletId, result.content().getFirst().getWalletId());
        verify(transactionRepository).findAllByTenantIdAndWalletIdInForRead(
                eq(TENANT_ID), eq(List.of(walletId)), eq(null), eq(null), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getAllTransactionsReturnsEmptyPageForUserWithoutAccessibleWallets() {
        authenticate(Role.USER);

        when(userWalletAccessService.getAccessibleWalletIds(any(UserPrincipal.class))).thenReturn(List.of());

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(null, null, null, null, 0, 20);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());
        assertTrue(result.last());
        assertFalse(result.hasNext());
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void getAllTransactionsReturnsEmptyPageWhenUserFiltersInaccessibleWallet() {
        authenticate(Role.USER);

        UUID accessibleWalletId = UUID.randomUUID();
        UUID filteredWalletId = UUID.randomUUID();
        when(userWalletAccessService.getAccessibleWalletIds(any(UserPrincipal.class))).thenReturn(List.of(accessibleWalletId));

        PageResponse<TransactionReadResponse> result = service.getAllTransactions(filteredWalletId, null, null, null, 0, 20);

        assertTrue(result.content().isEmpty());
        assertEquals(0, result.totalElements());
        verifyNoInteractions(transactionRepository);
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

    @Test
    void createTransactionCreatesLowOwnerNotificationsThenEvaluatesWalletConsumptionNotifications() {
        authenticate(Role.OWNER);

        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Wallet wallet = wallet(walletId);

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setWalletId(walletId);
        request.setExternalTransactionId("trx-123");
        request.setAmount(BigDecimal.valueOf(50));
        request.setType(TransactionType.CREDIT);
        request.setPercent(BigDecimal.valueOf(5));
        request.setPhoneNumber("0123456789");
        request.setDescription("topup");
        request.setCash(false);
        request.setOccurredAt(LocalDateTime.of(2026, 4, 30, 12, 0));

        when(walletRepository.findByIdAndTenantId(walletId, TENANT_ID)).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByTenantIdAndExternalTransactionId(TENANT_ID, "trx-123")).thenReturn(Optional.empty());
        when(transactionRepository.saveAndFlush(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setId(transactionId);
            saved.setCreatedAt(LocalDateTime.of(2026, 4, 30, 12, 1));
            saved.setUpdatedAt(LocalDateTime.of(2026, 4, 30, 12, 1));
            return saved;
        });

        TransactionResponse response = service.createTransaction(request);

        assertEquals(transactionId, response.getId());
        assertEquals(walletId, response.getWalletId());
        assertEquals(BigDecimal.valueOf(50), response.getAmount());

        org.mockito.InOrder inOrder = inOrder(
                transactionRepository,
                transactionNotificationService,
                walletRepository,
                walletConsumptionService,
                walletConsumptionNotificationService
        );
        inOrder.verify(transactionRepository).saveAndFlush(any(Transaction.class));
        inOrder.verify(transactionNotificationService).createTransactionCreatedNotifications(
                eq(wallet),
                argThat(candidate -> candidate != null && transactionId.equals(candidate.getId())),
                eq("current-user")
        );
        inOrder.verify(walletRepository).save(wallet);
        inOrder.verify(walletConsumptionService).applyTransaction(
                eq(wallet),
                argThat(candidate -> candidate != null && transactionId.equals(candidate.getId()))
        );
        inOrder.verify(walletConsumptionNotificationService).evaluateAndCreateWalletLimitNotifications(wallet, wallet.getConsumption());
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(USER_ID, "current-user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private Wallet wallet(UUID walletId) {
        Wallet wallet = Wallet.builder()
                .name("Main Wallet")
                .number("123")
                .balance(BigDecimal.valueOf(100))
                .active(true)
                .type(WalletType.Vodafone)
                .branchId(UUID.randomUUID())
                .dailyLimit(BigDecimal.valueOf(1000))
                .monthlyLimit(BigDecimal.valueOf(10000))
                .cashProfit(BigDecimal.ZERO)
                .walletProfit(BigDecimal.ZERO)
                .build();
        wallet.setId(walletId);
        wallet.setTenantId(TENANT_ID);
        wallet.setConsumption(WalletConsumption.builder()
                .wallet(wallet)
                .walletId(walletId)
                .dailyConsumed(BigDecimal.ZERO)
                .monthlyConsumed(BigDecimal.ZERO)
                .dailyWindowDate(LocalDate.of(2026, 4, 30))
                .monthlyWindowKey("2026-04")
                .isNew(false)
                .build());
        return wallet;
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
