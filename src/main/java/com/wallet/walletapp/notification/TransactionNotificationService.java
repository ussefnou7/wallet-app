package com.wallet.walletapp.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.transaction.Transaction;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.wallet.Wallet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransactionNotificationService {

    private static final String TARGET_TYPE_TRANSACTION = "TRANSACTION";

    private final UserRepository userRepository;
    private final NotificationCreationService notificationCreationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createTransactionCreatedNotifications(Wallet wallet, Transaction transaction, String createdByUsername) {
        List<UUID> ownerIds = findActiveOwnerIdsByTenantId(wallet.getTenantId());
        if (ownerIds.isEmpty()) {
            return;
        }

        String payloadJson = buildPayload(wallet, transaction, createdByUsername);
        for (UUID ownerId : ownerIds) {
            try {
                notificationCreationService.createIfNotExists(NotificationCreateCommand.builder()
                        .tenantId(wallet.getTenantId())
                        .recipientUserId(ownerId)
                        .type(NotificationType.TRANSACTION_CREATED)
                        .priority(NotificationPriority.LOW)
                        .severity(NotificationSeverity.INFO)
                        .titleKey("notifications.transactionCreated.title")
                        .messageKey("notifications.transactionCreated.message")
                        .payloadJson(payloadJson)
                        .targetType(TARGET_TYPE_TRANSACTION)
                        .targetId(transaction.getId())
                        .idempotencyKey("trx:%s:owner:%s".formatted(transaction.getId(), ownerId))
                        .build());
            } catch (Exception ex) {
                log.warn("Failed to create transaction notification. tenantId={}, transactionId={}, ownerId={}, type={}",
                        wallet.getTenantId(), transaction.getId(), ownerId, NotificationType.TRANSACTION_CREATED, ex);
            }
        }
    }

    List<UUID> findActiveOwnerIdsByTenantId(UUID tenantId) {
        return userRepository.findByTenantIdAndRole(tenantId, Role.OWNER).stream()
                .filter(User::isActive)
                .map(User::getId)
                .toList();
    }

    private String buildPayload(Wallet wallet, Transaction transaction, String createdByUsername) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("transactionId", transaction.getId());
        payload.put("walletId", wallet.getId());
        payload.put("walletName", wallet.getName());
        payload.put("amount", transaction.getAmount());
        payload.put("type", transaction.getType());
        if (createdByUsername != null && !createdByUsername.isBlank()) {
            payload.put("createdByUsername", createdByUsername);
        }
        return toJson(payload);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize notification payload", ex);
        }
    }
}
