package com.wallet.walletapp.notification;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.notification.dto.NotificationCountResponse;
import com.wallet.walletapp.notification.dto.NotificationResponse;
import com.wallet.walletapp.notification.dto.NotificationUnreadGroupedResponse;
import com.wallet.walletapp.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_LIMIT = 100;
    private static final int MAX_LIMIT = 100;

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public NotificationUnreadGroupedResponse getUnreadGrouped(Integer limit) {
        UserPrincipal user = currentOwner();
        int resolvedLimit = resolveLimit(limit);
        List<NotificationResponse> notifications = notificationRepository
                .findByTenantIdAndRecipientUserIdAndReadAtIsNullOrderByPriorityRankDescCreatedAtDesc(
                        user.getTenantId(),
                        user.getUserId(),
                        PageRequest.of(0, resolvedLimit)
                )
                .stream()
                .map(notificationMapper::toResponse)
                .toList();

        NotificationUnreadGroupedResponse response = new NotificationUnreadGroupedResponse();
        response.setUnreadCount(notificationRepository.countByTenantIdAndRecipientUserIdAndReadAtIsNull(user.getTenantId(), user.getUserId()));
        response.setImportant(notifications.stream()
                .filter(notification -> notification.getPriority() == NotificationPriority.HIGH
                        || notification.getPriority() == NotificationPriority.MEDIUM)
                .toList());
        response.setLow(notifications.stream()
                .filter(notification -> notification.getPriority() == NotificationPriority.LOW)
                .toList());
        return response;
    }

    @Transactional(readOnly = true)
    public NotificationCountResponse getUnreadCount() {
        UserPrincipal user = currentOwner();
        return new NotificationCountResponse(
                notificationRepository.countByTenantIdAndRecipientUserIdAndReadAtIsNull(user.getTenantId(), user.getUserId())
        );
    }

    @Transactional
    public void markAsRead(UUID id) {
        UserPrincipal user = currentOwner();
        Notification notification = notificationRepository.findByIdAndTenantIdAndRecipientUserId(id, user.getTenantId(), user.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, "Notification not found"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markLowAsRead() {
        UserPrincipal user = currentOwner();
        notificationRepository.markUnreadByPriorityAsRead(
                user.getTenantId(),
                user.getUserId(),
                NotificationPriority.LOW,
                LocalDateTime.now()
        );
    }

    @Transactional
    public void markAllAsRead() {
        UserPrincipal user = currentOwner();
        notificationRepository.markAllUnreadAsRead(
                user.getTenantId(),
                user.getUserId(),
                LocalDateTime.now()
        );
    }

    private UserPrincipal currentOwner() {
        UserPrincipal user = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user.getRole() != Role.OWNER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        return user;
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
