package com.wallet.walletapp.renewal;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.renewal.dto.CreateRenewalRequest;
import com.wallet.walletapp.renewal.dto.RenewalRequestResponse;
import com.wallet.walletapp.renewal.dto.ReviewRenewalRequest;
import com.wallet.walletapp.user.Role;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RenewalRequestServiceImpl implements RenewalRequestService {

    private final RenewalRequestRepository renewalRequestRepository;
    private final RenewalRequestMapper renewalRequestMapper;

    @Override
    @Transactional
    public RenewalRequestResponse createRequest(CreateRenewalRequest request) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.OWNER);

        RenewalRequest renewalRequest = RenewalRequest.builder()
                .requestedBy(user.getUserId())
                .phoneNumber(request.getPhoneNumber().trim())
                .amount(request.getAmount())
                .periodMonths(request.getPeriodMonths())
                .status(RenewalRequestStatus.PENDING)
                .build();
        renewalRequest.setTenantId(user.getTenantId());

        renewalRequest = renewalRequestRepository.save(renewalRequest);
        return renewalRequestMapper.toResponse(renewalRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenewalRequestResponse> getMyRequests() {
        UserPrincipal user = currentUser();
        requireRole(user, Role.OWNER);

        return renewalRequestRepository.findAllByTenantIdForRead(user.getTenantId()).stream()
                .map(renewalRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RenewalRequestResponse getMyRequest(UUID requestId) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.OWNER);

        RenewalRequestReadProjection request = renewalRequestRepository.findByIdAndTenantIdForRead(requestId, user.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RENEWAL_REQUEST_NOT_FOUND, "Renewal request not found"));
        return renewalRequestMapper.toResponse(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RenewalRequestResponse> getAdminRequests(@Nullable RenewalRequestStatus status,
                                                         @Nullable UUID tenantId) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.SYSTEM_ADMIN);

        return renewalRequestRepository.findAllForAdmin(tenantId, status).stream()
                .map(renewalRequestMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RenewalRequestResponse approveRequest(UUID requestId, @Nullable ReviewRenewalRequest request) {
        return reviewRequest(requestId, RenewalRequestStatus.APPROVED, request);
    }

    @Override
    @Transactional
    public RenewalRequestResponse rejectRequest(UUID requestId, @Nullable ReviewRenewalRequest request) {
        return reviewRequest(requestId, RenewalRequestStatus.REJECTED, request);
    }

    private RenewalRequestResponse reviewRequest(UUID requestId,
                                                 RenewalRequestStatus targetStatus,
                                                 @Nullable ReviewRenewalRequest reviewRequest) {
        UserPrincipal user = currentUser();
        requireRole(user, Role.SYSTEM_ADMIN);

        RenewalRequest renewalRequest = renewalRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RENEWAL_REQUEST_NOT_FOUND, "Renewal request not found"));

        if (renewalRequest.getStatus() != RenewalRequestStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_RENEWAL_REQUEST_STATUS);
        }

        renewalRequest.setStatus(targetStatus);
        renewalRequest.setReviewedAt(LocalDateTime.now());
        renewalRequest.setReviewedBy(user.getUserId());
        renewalRequest.setAdminNote(normalizeAdminNote(reviewRequest));

        renewalRequest = renewalRequestRepository.save(renewalRequest);
        return renewalRequestMapper.toResponse(renewalRequest);
    }

    private String normalizeAdminNote(@Nullable ReviewRenewalRequest request) {
        if (request == null || request.getAdminNote() == null) {
            return null;
        }

        String adminNote = request.getAdminNote().trim();
        return adminNote.isEmpty() ? null : adminNote;
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
