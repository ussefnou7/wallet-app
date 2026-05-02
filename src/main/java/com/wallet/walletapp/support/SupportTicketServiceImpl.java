package com.wallet.walletapp.support;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.support.dto.CreateSupportTicketRequest;
import com.wallet.walletapp.support.dto.SupportTicketResponse;
import com.wallet.walletapp.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final SupportTicketMapper supportTicketMapper;

    @Override
    @Transactional
    public SupportTicketResponse createTicket(CreateSupportTicketRequest request) {
        UserPrincipal user = currentUser();

        SupportTicket ticket = SupportTicket.builder()
                .createdBy(user.getUserId())
                .subject(request.getSubject().trim())
                .description(request.getDescription().trim())
                .type(request.getType() != null ? request.getType() : SupportTicketType.GENERAL)
                .priority(request.getPriority() != null ? request.getPriority() : SupportTicketPriority.MEDIUM)
                .status(SupportTicketStatus.OPEN)
                .build();
        ticket.setTenantId(user.getTenantId());

        ticket = supportTicketRepository.save(ticket);

        log.info("Support ticket {} created for tenant {}", ticket.getId(), ticket.getTenantId());
        return supportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getMyTickets() {
        UserPrincipal user = currentUser();

        List<SupportTicketReadProjection> tickets = supportTicketRepository.findAllByTenantIdAndUserIdForRead(user.getTenantId(),user.getUserId());
        return tickets.stream()
                .map(supportTicketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SupportTicketResponse getMyTicket(UUID ticketId) {
        UserPrincipal user = currentUser();

        SupportTicketReadProjection ticket = supportTicketRepository.findByIdAndTenantIdForRead(ticketId, user.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SUPPORT_TICKET_NOT_FOUND, "Support ticket not found"));
        return supportTicketMapper.toResponse(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getAdminTickets(@Nullable SupportTicketStatus status,
                                                       @Nullable UUID tenantId,
                                                       @Nullable SupportTicketPriority priority) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.SYSTEM_ADMIN);

        List<SupportTicketReadProjection> tickets = supportTicketRepository.findAllForAdmin(tenantId, status, priority);
        return tickets.stream()
                .map(supportTicketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SupportTicketResponse resolveTicket(UUID ticketId) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.SYSTEM_ADMIN);

        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.SUPPORT_TICKET_NOT_FOUND, "Support ticket not found"));

        if (ticket.getStatus() != SupportTicketStatus.RESOLVED) {
            ticket.setStatus(SupportTicketStatus.RESOLVED);
            ticket.setResolvedAt(LocalDateTime.now());
            ticket.setResolvedBy(user.getUserId());
            ticket = supportTicketRepository.save(ticket);
        }

        return supportTicketMapper.toResponse(ticket);
    }

    private void requireRole(UserPrincipal user, Role expectedRole) {
        if (user.getRole() != expectedRole) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
