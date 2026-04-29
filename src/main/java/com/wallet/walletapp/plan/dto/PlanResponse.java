package com.wallet.walletapp.plan.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PlanResponse {

    private UUID id;
    private String name;
    private BigDecimal price;
    private String description;
    private Integer maxUsers;
    private Integer maxWallets;
    private Integer maxBranches;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
