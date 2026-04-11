package com.wallet.walletapp.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateWalletRequest {

    @NotBlank
    private String name;

    private boolean active;
}
