package com.wallet.walletapp.support;

import com.wallet.walletapp.support.dto.SupportTicketResponse;
import org.springframework.stereotype.Component;

@Component
public class SupportTicketMapper {

    public SupportTicketResponse toResponse(SupportTicketReadProjection projection) {
        return new SupportTicketResponse(
                projection.getId(),
                projection.getTenantId(),
                projection.getCreatedBy(),
                projection.getSubject(),
                projection.getDescription(),
                projection.getPriority(),
                projection.getStatus(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getResolvedAt(),
                projection.getResolvedBy()
        );
    }

    public SupportTicketResponse toResponse(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getTenantId(),
                ticket.getCreatedBy(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getPriority(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getResolvedAt(),
                ticket.getResolvedBy()
        );
    }
}
