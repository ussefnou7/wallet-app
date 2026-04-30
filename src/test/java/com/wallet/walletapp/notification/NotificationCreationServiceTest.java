package com.wallet.walletapp.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationCreationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    void returnsTrueWhenInsertCreatesNotification() {
        when(notificationRepository.insertIgnoreIdempotencyConflict(
                any(), any(), any(), anyString(), anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(), isNull(), any(), any()))
                .thenReturn(1);

        boolean created = service().createIfNotExists(command());

        assertTrue(created);
        verify(notificationRepository).insertIgnoreIdempotencyConflict(
                any(), any(), any(), anyString(), anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(), isNull(), any(), any());
    }

    @Test
    void returnsFalseWhenInsertIsIgnoredByIdempotencyConflict() {
        when(notificationRepository.insertIgnoreIdempotencyConflict(
                any(), any(), any(), anyString(), anyString(), anyInt(), anyString(), anyString(),
                anyString(), anyString(), anyString(), any(), anyString(), isNull(), any(), any()))
                .thenReturn(0);

        boolean created = service().createIfNotExists(command());

        assertFalse(created);
    }

    private NotificationCreationService service() {
        return new NotificationCreationService(notificationRepository);
    }

    private NotificationCreateCommand command() {
        return NotificationCreateCommand.builder()
                .tenantId(UUID.randomUUID())
                .recipientUserId(UUID.randomUUID())
                .type(NotificationType.WALLET_DAILY_LIMIT_NEAR)
                .priority(NotificationPriority.MEDIUM)
                .severity(NotificationSeverity.WARNING)
                .titleKey("title")
                .messageKey("message")
                .payloadJson("{\"walletId\":\"123\"}")
                .targetType("WALLET")
                .targetId(UUID.randomUUID())
                .idempotencyKey("wallet:test")
                .build();
    }
}
