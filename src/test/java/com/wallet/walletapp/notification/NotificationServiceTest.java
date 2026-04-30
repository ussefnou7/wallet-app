package com.wallet.walletapp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.notification.dto.NotificationCountResponse;
import com.wallet.walletapp.notification.dto.NotificationUnreadGroupedResponse;
import com.wallet.walletapp.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");

    @Mock
    private NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper = new NotificationMapper(new ObjectMapper());

    @InjectMocks
    private NotificationService notificationService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUnreadGroupedSplitsImportantAndLowNotifications() {
        authenticate(Role.OWNER);

        Notification high = notification(UUID.randomUUID(), NotificationPriority.HIGH, null);
        Notification low = notification(UUID.randomUUID(), NotificationPriority.LOW, null);

        when(notificationRepository.findByTenantIdAndRecipientUserIdAndReadAtIsNullOrderByPriorityRankDescCreatedAtDesc(
                eq(TENANT_ID), eq(OWNER_ID), any(Pageable.class)))
                .thenReturn(List.of(high, low));
        when(notificationRepository.countByTenantIdAndRecipientUserIdAndReadAtIsNull(TENANT_ID, OWNER_ID))
                .thenReturn(2L);

        NotificationUnreadGroupedResponse response = service().getUnreadGrouped(150);

        assertEquals(2L, response.getUnreadCount());
        assertEquals(1, response.getImportant().size());
        assertEquals(1, response.getLow().size());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(notificationRepository).findByTenantIdAndRecipientUserIdAndReadAtIsNullOrderByPriorityRankDescCreatedAtDesc(
                eq(TENANT_ID), eq(OWNER_ID), pageableCaptor.capture());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getUnreadCountReturnsTenantScopedCount() {
        authenticate(Role.OWNER);
        when(notificationRepository.countByTenantIdAndRecipientUserIdAndReadAtIsNull(TENANT_ID, OWNER_ID))
                .thenReturn(7L);

        NotificationCountResponse response = service().getUnreadCount();

        assertEquals(7L, response.getCount());
    }

    @Test
    void markAsReadThrowsWhenNotificationDoesNotBelongToCurrentOwner() {
        authenticate(Role.OWNER);
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndTenantIdAndRecipientUserId(notificationId, TENANT_ID, OWNER_ID))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> service().markAsRead(notificationId));
    }

    @Test
    void markAsReadIsIdempotentWhenAlreadyRead() {
        authenticate(Role.OWNER);
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findByIdAndTenantIdAndRecipientUserId(notificationId, TENANT_ID, OWNER_ID))
                .thenReturn(Optional.of(notification(notificationId, NotificationPriority.MEDIUM, LocalDateTime.now())));

        service().markAsRead(notificationId);

        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void ownerRoleIsRequiredInServiceLayer() {
        authenticate(Role.USER);

        assertThrows(BusinessException.class, () -> service().getUnreadCount());
    }

    private NotificationService service() {
        return new NotificationService(notificationRepository, notificationMapper);
    }

    private void authenticate(Role role) {
        UserPrincipal principal = new UserPrincipal(OWNER_ID, "owner", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
        );
    }

    private Notification notification(UUID id, NotificationPriority priority, LocalDateTime readAt) {
        Notification notification = Notification.builder()
                .recipientUserId(OWNER_ID)
                .type(NotificationType.WALLET_DAILY_LIMIT_NEAR)
                .priority(priority)
                .priorityRank(priority.getRank())
                .severity(NotificationSeverity.WARNING)
                .titleKey("title")
                .messageKey("message")
                .payloadJson("{\"walletId\":\"123\",\"walletName\":\"Ops Wallet\",\"periodDate\":\"2026-04-30\"}")
                .readAt(readAt)
                .build();
        notification.setId(id);
        notification.setTenantId(TENANT_ID);
        notification.setCreatedAt(LocalDateTime.of(2026, 4, 30, 10, 0));
        return notification;
    }
}
