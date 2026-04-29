package com.wallet.walletapp.plan.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePlanRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer maxUsers;

    @NotNull
    @Min(0)
    private Integer maxWallets;

    @NotNull
    @Min(0)
    private Integer maxBranches;

    private boolean active = true;

    private BigDecimal price;
}
