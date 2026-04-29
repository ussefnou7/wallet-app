package com.wallet.walletapp.renewal;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface RenewalRequestReadProjection {

    UUID getId();

    UUID getTenantId();

    UUID getRequestedBy();

    String getPhoneNumber();

    BigDecimal getAmount();

    Integer getPeriodMonths();

    RenewalRequestStatus getStatus();

    LocalDateTime getReviewedAt();

    UUID getReviewedBy();

    String getAdminNote();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();

    String getRequestedByName();

    String getReviewedByName();

    String getTenantName();
}
