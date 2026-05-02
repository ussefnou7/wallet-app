package com.wallet.walletapp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.consumption.WalletConsumption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletConsumptionNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationCreationService notificationCreationService;

    private WalletConsumptionNotificationService walletConsumptionNotificationService;

    @BeforeEach
    void setUp() {
        walletConsumptionNotificationService =
                new WalletConsumptionNotificationService(userRepository, notificationCreationService, new ObjectMapper());
    }

    @Test
    void createsNearDailyAndExceededMonthlyNotificationsForActiveOwner() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        when(userRepository.findByTenantIdAndRole(tenantId, Role.OWNER))
                .thenReturn(List.of(activeOwner(ownerId, tenantId)));

        Wallet wallet = Wallet.builder()
                .name("Ops Wallet")
                .dailyLimit(BigDecimal.valueOf(100))
                .monthlyLimit(BigDecimal.valueOf(200))
                .build();
        wallet.setId(walletId);
        wallet.setTenantId(tenantId);

        WalletConsumption consumption = WalletConsumption.builder()
                .walletId(walletId)
                .dailyConsumed(BigDecimal.valueOf(85))
                .monthlyConsumed(BigDecimal.valueOf(220))
                .dailyWindowDate(LocalDate.of(2026, 4, 30))
                .monthlyWindowKey("2026-04")
                .isNew(false)
                .build();

        walletConsumptionNotificationService.evaluateAndCreateWalletLimitNotifications(wallet, consumption);

        ArgumentCaptor<NotificationCreateCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCreateCommand.class);
        verify(notificationCreationService, times(2)).createIfNotExists(commandCaptor.capture());

        List<NotificationCreateCommand> commands = commandCaptor.getAllValues();
        assertEquals("wallet:%s:DAILY_LIMIT_NEAR:2026-04-30:owner:%s".formatted(walletId, ownerId), commands.get(0).getIdempotencyKey());
        assertEquals(NotificationType.WALLET_DAILY_LIMIT_NEAR, commands.get(0).getType());
        assertEquals(NotificationPriority.MEDIUM, commands.get(0).getPriority());
        assertEquals(NotificationSeverity.WARNING, commands.get(0).getSeverity());
        assertEquals(Map.of(
                "walletId", walletId.toString(),
                "walletName", "Ops Wallet",
                "periodDate", "2026-04-30"
        ), readPayload(commands.get(0).getPayloadJson()));

        assertEquals("wallet:%s:MONTHLY_LIMIT_EXCEEDED:2026-04:owner:%s".formatted(walletId, ownerId), commands.get(1).getIdempotencyKey());
        assertEquals(NotificationType.WALLET_MONTHLY_LIMIT_EXCEEDED, commands.get(1).getType());
        assertEquals(NotificationPriority.HIGH, commands.get(1).getPriority());
        assertEquals(NotificationSeverity.DANGER, commands.get(1).getSeverity());
        assertEquals(Map.of(
                "walletId", walletId.toString(),
                "walletName", "Ops Wallet",
                "periodMonth", "2026-04"
        ), readPayload(commands.get(1).getPayloadJson()));
    }

    @Test
    void skipsNotificationCreationWhenNoActiveOwnersExist() {
        UUID tenantId = UUID.randomUUID();
        when(userRepository.findByTenantIdAndRole(tenantId, Role.OWNER))
                .thenReturn(List.of(inactiveOwner(tenantId)));

        Wallet wallet = Wallet.builder()
                .name("Dormant Wallet")
                .dailyLimit(BigDecimal.valueOf(100))
                .monthlyLimit(BigDecimal.valueOf(100))
                .build();
        wallet.setId(UUID.randomUUID());
        wallet.setTenantId(tenantId);

        WalletConsumption consumption = WalletConsumption.builder()
                .walletId(wallet.getId())
                .dailyConsumed(BigDecimal.valueOf(90))
                .monthlyConsumed(BigDecimal.valueOf(90))
                .dailyWindowDate(LocalDate.of(2026, 4, 30))
                .monthlyWindowKey("2026-04")
                .isNew(false)
                .build();

        walletConsumptionNotificationService.evaluateAndCreateWalletLimitNotifications(wallet, consumption);

        verify(notificationCreationService, times(0)).createIfNotExists(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void swallowsNotificationCreationFailuresPerOwner() {
        UUID tenantId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();

        when(userRepository.findByTenantIdAndRole(tenantId, Role.OWNER))
                .thenReturn(List.of(activeOwner(ownerId, tenantId)));
        when(notificationCreationService.createIfNotExists(any()))
                .thenThrow(new RuntimeException("insert failed"));

        Wallet wallet = Wallet.builder()
                .name("Ops Wallet")
                .dailyLimit(BigDecimal.valueOf(100))
                .monthlyLimit(BigDecimal.valueOf(200))
                .build();
        wallet.setId(walletId);
        wallet.setTenantId(tenantId);

        WalletConsumption consumption = WalletConsumption.builder()
                .walletId(walletId)
                .dailyConsumed(BigDecimal.valueOf(85))
                .monthlyConsumed(BigDecimal.ZERO)
                .dailyWindowDate(LocalDate.of(2026, 4, 30))
                .monthlyWindowKey("2026-04")
                .isNew(false)
                .build();

        walletConsumptionNotificationService.evaluateAndCreateWalletLimitNotifications(wallet, consumption);

        verify(notificationCreationService, times(1)).createIfNotExists(any());
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

    private User inactiveOwner(UUID tenantId) {
        User user = User.builder()
                .username("inactive-owner")
                .password("password")
                .role(Role.OWNER)
                .active(false)
                .build();
        user.setId(UUID.randomUUID());
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
