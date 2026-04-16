package com.wallet.walletapp.plan.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class AssignTenantSubscriptionRequest {

    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID planId;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate expireDate;
}
