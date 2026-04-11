package com.wallet.walletapp.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBranchRequest {

    @NotNull
    private UUID tenantId;

    @NotBlank
    private String name;
}
