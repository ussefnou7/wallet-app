package com.wallet.walletapp.transaction.dto;

import com.wallet.walletapp.transaction.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateTransactionRequest {

    @NotNull
    private UUID walletId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotNull
    private TransactionType type;

    @DecimalMin(value = "0.0")
    private BigDecimal percent = BigDecimal.ZERO;

    @NotNull
    private String phoneNumber;

    private String description;

    private boolean isCash;
}
