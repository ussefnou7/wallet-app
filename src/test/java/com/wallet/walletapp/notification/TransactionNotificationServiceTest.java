package com.wallet.walletapp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.transaction.Transaction;
import com.wallet.walletapp.transaction.TransactionType;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.WalletType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationCreationService notificationCreationService;

    private TransactionNotificationService transactionNotificationService;

    @BeforeEach
    void setUp() {
        transactionNotificationService =
                new TransactionNotificationService(userRepository, notificationCreationService, new ObjectMapper());
    }

    @Test
    void createsLowPriorityTransactionNotificationsForActiveOwners() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        when(userRepository.findByTenantIdAndRole(tenantId, Role.OWNER))
                .thenReturn(List.of(activeOwner(ownerId, tenantId)));

        Wallet wallet = Wallet.builder()
                .name("Ops Wallet")
                .number("123")
                .balance(BigDecimal.ZERO)
                .active(true)
                .type(WalletType.Vodafone)
                .branchId(UUID.randomUUID())
                .dailyLimit(BigDecimal.ZERO)
                .monthlyLimit(BigDecimal.ZERO)
                .cashProfit(BigDecimal.ZERO)
                .walletProfit(BigDecimal.ZERO)
                .build();
        wallet.setId(walletId);
        wallet.setTenantId(tenantId);

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .externalTransactionId("trx-123")
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.CREDIT)
                .percent(BigDecimal.ZERO)
                .phoneNumber("0123456789")
                .isCash(false)
                .build();
        transaction.setId(transactionId);
        transaction.setTenantId(tenantId);

        transactionNotificationService.createTransactionCreatedNotifications(wallet, transaction, "creator");

        ArgumentCaptor<NotificationCreateCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCreateCommand.class);
        verify(notificationCreationService, times(1)).createIfNotExists(commandCaptor.capture());

        NotificationCreateCommand command = commandCaptor.getValue();
        assertEquals(NotificationType.TRANSACTION_CREATED, command.getType());
        assertEquals(NotificationPriority.LOW, command.getPriority());
        assertEquals(NotificationSeverity.INFO, command.getSeverity());
        assertEquals("TRANSACTION", command.getTargetType());
        assertEquals(transactionId, command.getTargetId());
        assertEquals("trx:%s:owner:%s".formatted(transactionId, ownerId), command.getIdempotencyKey());
        assertEquals(Map.of(
                "transactionId", transactionId.toString(),
                "walletId", walletId.toString(),
                "walletName", "Ops Wallet",
                "amount", 50,
                "type", "CREDIT",
                "createdByUsername", "creator"
        ), readPayload(command.getPayloadJson()));
    }

    @Test
    void omitsCreatedByUsernameWhenUnavailable() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        when(userRepository.findByTenantIdAndRole(tenantId, Role.OWNER))
                .thenReturn(List.of(activeOwner(ownerId, tenantId)));

        Wallet wallet = Wallet.builder()
                .name("Ops Wallet")
                .number("123")
                .balance(BigDecimal.ZERO)
                .active(true)
                .type(WalletType.Vodafone)
                .branchId(UUID.randomUUID())
                .dailyLimit(BigDecimal.ZERO)
                .monthlyLimit(BigDecimal.ZERO)
                .cashProfit(BigDecimal.ZERO)
                .walletProfit(BigDecimal.ZERO)
                .build();
        wallet.setId(walletId);
        wallet.setTenantId(tenantId);

        Transaction transaction = Transaction.builder()
                .walletId(walletId)
                .externalTransactionId("trx-123")
                .amount(BigDecimal.valueOf(50))
                .type(TransactionType.CREDIT)
                .percent(BigDecimal.ZERO)
                .phoneNumber("0123456789")
                .isCash(false)
                .build();
        transaction.setId(transactionId);
        transaction.setTenantId(tenantId);

        transactionNotificationService.createTransactionCreatedNotifications(wallet, transaction, null);

        ArgumentCaptor<NotificationCreateCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCreateCommand.class);
        verify(notificationCreationService).createIfNotExists(commandCaptor.capture());

        Map<String, Object> payload = readPayload(commandCaptor.getValue().getPayloadJson());
        assertFalse(payload.containsKey("createdByUsername"));
    }

    private User activeOwner(UUID ownerId, UUID tenantId) {
        User user = User.builder()
                .username("owner")
                .password("password")
                .role(Role.OWNER)
                .active(true)
                .build();
        user.setId(ownerId);
        user.setTenantId(tenantId);
        return user;
    }

    private Map<String, Object> readPayload(String payloadJson) {
        try {
            return new ObjectMapper().readValue(payloadJson, Map.class);
        } catch (Exception ex) {
            throw new AssertionError("Failed to parse payload json", ex);
        }
    }
}
