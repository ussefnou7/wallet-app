package com.wallet.walletapp.notification.dto;

import lombok.Data;

import java.util.List;

@Data
public class NotificationUnreadGroupedResponse {

    private long unreadCount;
    private List<NotificationResponse> important;
    private List<NotificationResponse> low;
}
