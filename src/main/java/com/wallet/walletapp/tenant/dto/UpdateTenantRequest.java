package com.wallet.walletapp.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateTenantRequest {

    @NotBlank
    private String name;

    private String phoneNumber;

    private boolean active;
}
