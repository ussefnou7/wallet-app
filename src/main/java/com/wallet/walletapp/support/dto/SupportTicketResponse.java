package com.wallet.walletapp.support.dto;

import com.wallet.walletapp.support.SupportTicketPriority;
import com.wallet.walletapp.support.SupportTicketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicketResponse {

    private UUID ticketId;
    private UUID tenantId;
    private UUID createdBy;
    private String subject;
    private String description;
    private SupportTicketPriority priority;
    private SupportTicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime resolvedAt;
    private UUID resolvedBy;
    private String tenantName;
    private String createdByName;
}
