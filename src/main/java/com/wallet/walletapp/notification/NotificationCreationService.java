package com.wallet.walletapp.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationCreationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public boolean createIfNotExists(NotificationCreateCommand command) {
        LocalDateTime now = LocalDateTime.now();
        return notificationRepository.insertIgnoreIdempotencyConflict(
                UUID.randomUUID(),
                command.getTenantId(),
                command.getRecipientUserId(),
                command.getType().name(),
                command.getPriority().name(),
                command.getPriority().getRank(),
                command.getSeverity().name(),
                command.getTitleKey(),
                command.getMessageKey(),
                command.getPayloadJson(),
                command.getTargetType(),
                command.getTargetId(),
                command.getIdempotencyKey(),
                null,
                now,
                now
        ) == 1;
    }
}
