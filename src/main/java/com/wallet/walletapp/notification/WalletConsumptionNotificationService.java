package com.wallet.walletapp.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.wallet.Wallet;
import com.wallet.walletapp.wallet.consumption.WalletConsumption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class WalletConsumptionNotificationService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal NEAR_THRESHOLD = BigDecimal.valueOf(80);
    private static final String TARGET_TYPE_WALLET = "WALLET";
    private static final DateTimeFormatter DAILY_KEY_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter MONTHLY_KEY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    private final UserRepository userRepository;
    private final NotificationCreationService notificationCreationService;
    private final ObjectMapper objectMapper;

    @Transactional
    public void evaluateAndCreateWalletLimitNotifications(Wallet wallet, WalletConsumption walletConsumption) {
        List<UUID> ownerIds = findActiveOwnerIdsByTenantId(wallet.getTenantId());
        if (ownerIds.isEmpty()) {
            return;
        }

        BigDecimal dailyPercent = calculateUsagePercent(walletConsumption.getDailyConsumed(), wallet.getDailyLimit());
        BigDecimal monthlyPercent = calculateUsagePercent(walletConsumption.getMonthlyConsumed(), wallet.getMonthlyLimit());

        if (dailyPercent.compareTo(ONE_HUNDRED) >= 0) {
            createForOwners(ownerIds, wallet, NotificationType.WALLET_DAILY_LIMIT_EXCEEDED, NotificationPriority.HIGH,
                    NotificationSeverity.DANGER, "notifications.walletDailyLimitExceeded.title",
                    "notifications.walletDailyLimitExceeded.message", buildDailyPayload(wallet, walletConsumption),
                    "wallet:%s:DAILY_LIMIT_EXCEEDED:%s".formatted(wallet.getId(), walletConsumption.getDailyWindowDate().format(DAILY_KEY_FORMAT)));
        } else if (dailyPercent.compareTo(NEAR_THRESHOLD) >= 0) {
            createForOwners(ownerIds, wallet, NotificationType.WALLET_DAILY_LIMIT_NEAR, NotificationPriority.MEDIUM,
                    NotificationSeverity.WARNING, "notifications.walletDailyLimitNear.title",
                    "notifications.walletDailyLimitNear.message", buildDailyPayload(wallet, walletConsumption),
                    "wallet:%s:DAILY_LIMIT_NEAR:%s".formatted(wallet.getId(), walletConsumption.getDailyWindowDate().format(DAILY_KEY_FORMAT)));
        }

        if (monthlyPercent.compareTo(ONE_HUNDRED) >= 0) {
            createForOwners(ownerIds, wallet, NotificationType.WALLET_MONTHLY_LIMIT_EXCEEDED, NotificationPriority.HIGH,
                    NotificationSeverity.DANGER, "notifications.walletMonthlyLimitExceeded.title",
                    "notifications.walletMonthlyLimitExceeded.message", buildMonthlyPayload(wallet, walletConsumption),
                    "wallet:%s:MONTHLY_LIMIT_EXCEEDED:%s".formatted(wallet.getId(), walletConsumption.getMonthlyWindowKey()));
        } else if (monthlyPercent.compareTo(NEAR_THRESHOLD) >= 0) {
            createForOwners(ownerIds, wallet, NotificationType.WALLET_MONTHLY_LIMIT_NEAR, NotificationPriority.MEDIUM,
                    NotificationSeverity.WARNING, "notifications.walletMonthlyLimitNear.title",
                    "notifications.walletMonthlyLimitNear.message", buildMonthlyPayload(wallet, walletConsumption),
                    "wallet:%s:MONTHLY_LIMIT_NEAR:%s".formatted(wallet.getId(), walletConsumption.getMonthlyWindowKey()));
        }
    }

    List<UUID> findActiveOwnerIdsByTenantId(UUID tenantId) {
        return userRepository.findByTenantIdAndRole(tenantId, Role.OWNER).stream()
                .filter(User::isActive)
                .map(User::getId)
                .toList();
    }

    private void createForOwners(List<UUID> ownerIds,
                                 Wallet wallet,
                                 NotificationType type,
                                 NotificationPriority priority,
                                 NotificationSeverity severity,
                                 String titleKey,
                                 String messageKey,
                                 String payloadJson,
                                 String idempotencyKey) {
        for (UUID ownerId : ownerIds) {
            try {
                notificationCreationService.createIfNotExists(NotificationCreateCommand.builder()
                        .tenantId(wallet.getTenantId())
                        .recipientUserId(ownerId)
                        .type(type)
                        .priority(priority)
                        .severity(severity)
                        .titleKey(titleKey)
                        .messageKey(messageKey)
                        .payloadJson(payloadJson)
                        .targetType(TARGET_TYPE_WALLET)
                        .targetId(wallet.getId())
                        .idempotencyKey(idempotencyKey + ":owner:" + ownerId)
                        .build());
            } catch (Exception ex) {
                log.warn("Failed to create notification. tenantId={}, walletId={}, ownerId={}, type={}",
                        wallet.getTenantId(), wallet.getId(), ownerId, type, ex);
            }
        }
    }

    private String buildDailyPayload(Wallet wallet, WalletConsumption walletConsumption) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("walletId", wallet.getId());
        payload.put("walletName", wallet.getName());
        payload.put("periodDate", walletConsumption.getDailyWindowDate().format(DAILY_KEY_FORMAT));
        return toJson(payload);
    }

    private String buildMonthlyPayload(Wallet wallet, WalletConsumption walletConsumption) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("walletId", wallet.getId());
        payload.put("walletName", wallet.getName());
        payload.put("periodMonth", walletConsumption.getMonthlyWindowKey());
        return toJson(payload);
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize notification payload", ex);
        }
    }

    private BigDecimal calculateUsagePercent(BigDecimal spent, BigDecimal limit) {
        if (limit == null || limit.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal safeSpent = spent != null ? spent : BigDecimal.ZERO;
        return safeSpent.multiply(ONE_HUNDRED).divide(limit, 2, RoundingMode.HALF_UP);
    }
}
