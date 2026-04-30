package com.wallet.walletapp.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateWalletRequest {

    @NotBlank
    private String name;

    private boolean active;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;
}
