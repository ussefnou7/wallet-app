package com.wallet.walletapp.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private final NotificationRepository notificationRepository;

    @Transactional
    @Scheduled(cron = "0 0 3 * * *", zone = "Africa/Cairo")
    public void deleteReadNotificationsOlderThanOneDay() {
        int deleted = notificationRepository.deleteReadOlderThan(LocalDateTime.now().minusDays(1));
        if (deleted > 0) {
            log.info("Deleted {} read notifications older than one day", deleted);
        }
    }
}
