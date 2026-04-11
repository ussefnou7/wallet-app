package com.wallet.walletapp.transaction.dto;

import com.wallet.walletapp.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionResponse {

    private UUID id;
    private UUID tenantId;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType type;
    private BigDecimal percent;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
