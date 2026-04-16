package com.wallet.walletapp.plan.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TenantSubscriptionResponse {

    private UUID id;
    private UUID tenantId;
    private UUID planId;
    private String planName;
    private LocalDate startDate;
    private LocalDate expireDate;
    private boolean valid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
