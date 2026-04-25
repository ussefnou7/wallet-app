package com.wallet.walletapp.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionReadProjection {
    UUID getId();

    UUID getTenantId();

    UUID getWalletId();

    String getWalletName();

    String getExternalTransactionId();

    BigDecimal getAmount();

    TransactionType getType();

    BigDecimal getPercent();

    String getPhoneNumber();

    Boolean getCash();

    String getDescription();

    LocalDateTime getOccurredAt();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    UUID getCreatedBy();

    String getCreatedByUsername();
}
