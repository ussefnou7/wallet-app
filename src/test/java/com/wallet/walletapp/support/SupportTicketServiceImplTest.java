package com.wallet.walletapp.support;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.support.dto.CreateSupportTicketRequest;
import com.wallet.walletapp.support.dto.SupportTicketResponse;
import com.wallet.walletapp.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Mock
    private SupportTicketRepository supportTicketRepository;

    private SupportTicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SupportTicketServiceImpl(
                supportTicketRepository,
                new SupportTicketMapper()
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createTicketStoresTenantScopedTicket() {
        authenticate(OWNER_ID, Role.OWNER);

        CreateSupportTicketRequest request = new CreateSupportTicketRequest();
        request.setSubject("Payment issue");
        request.setDescription("Need help with settlement");
        request.setPriority(SupportTicketPriority.HIGH);

        SupportTicket saved = ticket(UUID.randomUUID(), SupportTicketPriority.HIGH, SupportTicketStatus.OPEN);
        saved.setSubject("Payment issue");
        saved.setDescription("Need help with settlement");
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(saved);

        SupportTicketResponse response = service.createTicket(request);

        assertEquals(saved.getId(), response.getTicketId());
        assertEquals(TENANT_ID, response.getTenantId());
        assertEquals(OWNER_ID, response.getCreatedBy());
        assertEquals("Payment issue", response.getSubject());
        assertEquals(SupportTicketType.GENERAL, response.getType());
        assertEquals(SupportTicketPriority.HIGH, response.getPriority());
        assertEquals(SupportTicketStatus.OPEN, response.getStatus());
        ArgumentCaptor<SupportTicket> ticketCaptor = ArgumentCaptor.forClass(SupportTicket.class);
        verify(supportTicketRepository).save(ticketCaptor.capture());
        SupportTicket persisted = ticketCaptor.getValue();
        assertEquals(TENANT_ID, persisted.getTenantId());
        assertEquals(OWNER_ID, persisted.getCreatedBy());
        assertEquals(SupportTicketType.GENERAL, persisted.getType());
    }

    @Test
    void createTicketDefaultsPriorityToMedium() {
        authenticate(OWNER_ID, Role.OWNER);

        CreateSupportTicketRequest request = new CreateSupportTicketRequest();
        request.setSubject("Wallet screenshot");
        request.setDescription("Please check the ticket");
        request.setPriority(null);

        UUID ticketId = UUID.randomUUID();
        SupportTicket saved = ticket(ticketId, SupportTicketPriority.MEDIUM, SupportTicketStatus.OPEN);
        when(supportTicketRepository.save(any(SupportTicket.class))).thenReturn(saved);

        SupportTicketResponse response = service.createTicket(request);

        assertEquals(ticketId, response.getTicketId());
        assertEquals(SupportTicketType.GENERAL, response.getType());
        assertEquals(SupportTicketPriority.MEDIUM, response.getPriority());
        verify(supportTicketRepository).save(any(SupportTicket.class));
    }

    @Test
    void resolveTicketMarksTicketResolved() {
        authenticate(ADMIN_ID, Role.SYSTEM_ADMIN);

        UUID ticketId = UUID.randomUUID();
        SupportTicket ticket = ticket(ticketId, SupportTicketPriority.LOW, SupportTicketStatus.OPEN);
        when(supportTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(ticket)).thenReturn(ticket);

        SupportTicketResponse response = service.resolveTicket(ticketId);

        assertEquals(SupportTicketStatus.RESOLVED, response.getStatus());
        assertEquals(ADMIN_ID, response.getResolvedBy());
        assertNotNull(response.getResolvedAt());
        verify(supportTicketRepository).save(ticket);
    }

    private void authenticate(UUID userId, Role role) {
        UserPrincipal principal = new UserPrincipal(userId, "current-user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private SupportTicket ticket(UUID ticketId, SupportTicketPriority priority, SupportTicketStatus status) {
        SupportTicket ticket = new SupportTicket();
        ticket.setId(ticketId);
        ticket.setTenantId(TENANT_ID);
        ticket.setCreatedBy(OWNER_ID);
        ticket.setSubject("Subject");
        ticket.setDescription("Description");
        ticket.setType(SupportTicketType.GENERAL);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        ticket.setCreatedAt(LocalDateTime.of(2026, 4, 25, 15, 0));
        ticket.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 15, 5));
        return ticket;
    }
}
