package com.wallet.walletapp.notification;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class NotificationCreateCommand {

    private final UUID tenantId;
    private final UUID recipientUserId;
    private final NotificationType type;
    private final NotificationPriority priority;
    private final NotificationSeverity severity;
    private final String titleKey;
    private final String messageKey;
    private final String payloadJson;
    private final String targetType;
    private final UUID targetId;
    private final String idempotencyKey;
}
