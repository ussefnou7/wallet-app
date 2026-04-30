package com.wallet.walletapp.notification.dto;

import com.wallet.walletapp.notification.NotificationPriority;
import com.wallet.walletapp.notification.NotificationSeverity;
import com.wallet.walletapp.notification.NotificationType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NotificationResponse {

    private UUID id;
    private NotificationType type;
    private NotificationPriority priority;
    private NotificationSeverity severity;
    private String titleKey;
    private String messageKey;
    private Object payload;
    private String targetType;
    private UUID targetId;
    private LocalDateTime createdAt;
}
