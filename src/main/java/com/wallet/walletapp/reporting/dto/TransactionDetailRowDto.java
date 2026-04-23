package com.wallet.walletapp.reporting.dto;

import com.wallet.walletapp.transaction.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransactionDetailRowDto {
    private UUID id;
    private UUID walletId;
    private BigDecimal amount;
    private TransactionType type;
    private BigDecimal percent;
    private String phoneNumber;
    private Boolean cash;
    private String description;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
