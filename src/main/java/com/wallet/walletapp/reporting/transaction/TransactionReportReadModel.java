package com.wallet.walletapp.reporting.transaction;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.wallet.walletapp.transaction.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@JsonPropertyOrder({
        "tenantName",
        "walletName",
        "createdByUsername",
        "amount",
        "type",
        "percent",
        "phoneNumber",
        "cash",
        "description",
        "occurredAt",
        "createdAt",
        "transactionId",
        "tenantId",
        "walletId",
        "createdByUserId"
})
public class TransactionReportReadModel {

    private final UUID transactionId;
    private final UUID tenantId;
    private final String tenantName;
    private final UUID walletId;
    private final String walletName;
    private final UUID createdByUserId;
    private final String createdByUsername;
    private final BigDecimal amount;
    private final TransactionType type;
    private final BigDecimal percent;
    private final String phoneNumber;
    private final Boolean cash;
    private final String description;
    private final LocalDateTime occurredAt;
    private final LocalDateTime createdAt;

    public TransactionReportReadModel(UUID transactionId,
                                      UUID tenantId,
                                      String tenantName,
                                      UUID walletId,
                                      String walletName,
                                      UUID createdByUserId,
                                      String createdByUsername,
                                      BigDecimal amount,
                                      TransactionType type,
                                      BigDecimal percent,
                                      String phoneNumber,
                                      Boolean cash,
                                      String description,
                                      LocalDateTime occurredAt,
                                      LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.walletId = walletId;
        this.walletName = walletName;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.amount = amount;
        this.type = type;
        this.percent = percent;
        this.phoneNumber = phoneNumber;
        this.cash = cash;
        this.description = description;
        this.occurredAt = occurredAt;
        this.createdAt = createdAt;
    }
}
