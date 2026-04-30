package com.wallet.walletapp.notification;

import com.wallet.walletapp.notification.dto.NotificationCountResponse;
import com.wallet.walletapp.notification.dto.NotificationUnreadGroupedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/unread")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<NotificationUnreadGroupedResponse> getUnread(@RequestParam(required = false) Integer limit) {
        return ResponseEntity.ok(notificationService.getUnreadGrouped(limit));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<NotificationCountResponse> getUnreadCount() {
        return ResponseEntity.ok(notificationService.getUnreadCount());
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-low")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> markLowAsRead() {
        notificationService.markLowAsRead();
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
