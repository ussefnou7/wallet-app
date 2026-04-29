package com.wallet.walletapp.renewal.dto;

import com.wallet.walletapp.renewal.RenewalRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RenewalRequestResponse {

    private UUID requestId;
    private UUID tenantId;
    private UUID requestedBy;
    private String phoneNumber;
    private BigDecimal amount;
    private Integer periodMonths;
    private RenewalRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime reviewedAt;
    private UUID reviewedBy;
    private String adminNote;
    private String tenantName;
    private String requestedByName;
    private String reviewedByName;
}
