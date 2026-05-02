package com.wallet.walletapp.support;

import com.wallet.walletapp.support.dto.CreateSupportTicketRequest;
import com.wallet.walletapp.support.dto.SupportTicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    @PostMapping("/api/v1/support/tickets")
    public ResponseEntity<SupportTicketResponse> createTicket(@Valid @RequestBody CreateSupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supportTicketService.createTicket(request));
    }

    @GetMapping("/api/v1/support/tickets/my")
    public ResponseEntity<List<SupportTicketResponse>> getMyTickets() {
        return ResponseEntity.ok(supportTicketService.getMyTickets());
    }

    @GetMapping("/api/v1/support/tickets/{ticketId}")
    public ResponseEntity<SupportTicketResponse> getMyTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportTicketService.getMyTicket(ticketId));
    }

    @GetMapping("/api/v1/admin/support/tickets")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<SupportTicketResponse>> getAdminTickets(
            @RequestParam(required = false) SupportTicketStatus status,
            @RequestParam(required = false) UUID tenantId,
            @RequestParam(required = false) SupportTicketPriority priority) {
        return ResponseEntity.ok(supportTicketService.getAdminTickets(status, tenantId, priority));
    }

    @PatchMapping("/api/v1/admin/support/tickets/{ticketId}/resolve")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SupportTicketResponse> resolveTicket(@PathVariable UUID ticketId) {
        return ResponseEntity.ok(supportTicketService.resolveTicket(ticketId));
    }
}
