package com.wallet.walletapp.support;

import com.wallet.walletapp.support.dto.CreateSupportTicketRequest;
import com.wallet.walletapp.support.dto.SupportTicketResponse;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface SupportTicketService {

    SupportTicketResponse createTicket(CreateSupportTicketRequest request);

    List<SupportTicketResponse> getMyTickets();

    SupportTicketResponse getMyTicket(UUID ticketId);

    List<SupportTicketResponse> getAdminTickets(@Nullable SupportTicketStatus status,
                                                @Nullable UUID tenantId,
                                                @Nullable SupportTicketPriority priority);

    SupportTicketResponse resolveTicket(UUID ticketId);
}
