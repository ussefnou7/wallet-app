package com.wallet.walletapp.notification;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    private final int rank;
}
