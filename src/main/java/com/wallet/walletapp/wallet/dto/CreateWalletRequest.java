package com.wallet.walletapp.wallet.dto;

import com.wallet.walletapp.wallet.WalletType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateWalletRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String number;

    @NotNull
    private UUID tenantId;

    @NotNull
    private UUID branchId;

    @NotNull
    private WalletType type;

    @NotNull
    private BigDecimal balance;

    @NotNull
    BigDecimal dailyLimit = BigDecimal.ZERO;

    @NotNull
    BigDecimal monthlyLimit = BigDecimal.ZERO;

    private boolean active;
}
