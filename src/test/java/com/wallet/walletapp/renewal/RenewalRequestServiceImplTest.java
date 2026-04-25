package com.wallet.walletapp.renewal;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.renewal.dto.CreateRenewalRequest;
import com.wallet.walletapp.renewal.dto.RenewalRequestResponse;
import com.wallet.walletapp.renewal.dto.ReviewRenewalRequest;
import com.wallet.walletapp.user.Role;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RenewalRequestServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OWNER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");

    @Mock
    private RenewalRequestRepository renewalRequestRepository;

    private RenewalRequestServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RenewalRequestServiceImpl(
                renewalRequestRepository,
                new RenewalRequestMapper()
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerCanCreateRequest() {
        authenticate(OWNER_ID, Role.OWNER);

        CreateRenewalRequest request = new CreateRenewalRequest();
        request.setPhoneNumber("01012345678");
        request.setAmount(new BigDecimal("500.00"));
        request.setPeriodMonths(3);

        RenewalRequest saved = request(UUID.randomUUID(), RenewalRequestStatus.PENDING);
        saved.setPhoneNumber("01012345678");
        saved.setAmount(new BigDecimal("500.00"));
        saved.setPeriodMonths(3);
        when(renewalRequestRepository.save(any(RenewalRequest.class))).thenReturn(saved);

        RenewalRequestResponse response = service.createRequest(request);

        assertEquals(saved.getId(), response.getRequestId());
        assertEquals(TENANT_ID, response.getTenantId());
        assertEquals(OWNER_ID, response.getRequestedBy());
        assertEquals("01012345678", response.getPhoneNumber());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(3, response.getPeriodMonths());
        assertEquals(RenewalRequestStatus.PENDING, response.getStatus());
        verify(renewalRequestRepository).save(any(RenewalRequest.class));
    }

    @Test
    void ownerCanListOwnRequests() {
        authenticate(OWNER_ID, Role.OWNER);

        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        when(renewalRequestRepository.findAllByTenantIdForRead(TENANT_ID)).thenReturn(List.of(
                projection(firstId, RenewalRequestStatus.PENDING, LocalDateTime.of(2026, 4, 25, 18, 0)),
                projection(secondId, RenewalRequestStatus.APPROVED, LocalDateTime.of(2026, 4, 24, 18, 0))
        ));

        List<RenewalRequestResponse> responses = service.getMyRequests();

        assertEquals(2, responses.size());
        assertEquals(firstId, responses.get(0).getRequestId());
        assertEquals(secondId, responses.get(1).getRequestId());
        verify(renewalRequestRepository).findAllByTenantIdForRead(TENANT_ID);
    }

    @Test
    void adminCanApproveRequest() {
        authenticate(ADMIN_ID, Role.SYSTEM_ADMIN);

        UUID requestId = UUID.randomUUID();
        RenewalRequest renewalRequest = request(requestId, RenewalRequestStatus.PENDING);
        when(renewalRequestRepository.findById(requestId)).thenReturn(Optional.of(renewalRequest));
        when(renewalRequestRepository.save(renewalRequest)).thenReturn(renewalRequest);

        ReviewRenewalRequest reviewRequest = new ReviewRenewalRequest();
        reviewRequest.setAdminNote("Approved after manual review");

        RenewalRequestResponse response = service.approveRequest(requestId, reviewRequest);

        assertEquals(RenewalRequestStatus.APPROVED, response.getStatus());
        assertEquals(ADMIN_ID, response.getReviewedBy());
        assertEquals("Approved after manual review", response.getAdminNote());
        assertNotNull(response.getReviewedAt());
        verify(renewalRequestRepository).save(renewalRequest);
    }

    @Test
    void adminCanRejectRequest() {
        authenticate(ADMIN_ID, Role.SYSTEM_ADMIN);

        UUID requestId = UUID.randomUUID();
        RenewalRequest renewalRequest = request(requestId, RenewalRequestStatus.PENDING);
        when(renewalRequestRepository.findById(requestId)).thenReturn(Optional.of(renewalRequest));
        when(renewalRequestRepository.save(renewalRequest)).thenReturn(renewalRequest);

        RenewalRequestResponse response = service.rejectRequest(requestId, null);

        assertEquals(RenewalRequestStatus.REJECTED, response.getStatus());
        assertEquals(ADMIN_ID, response.getReviewedBy());
        assertNull(response.getAdminNote());
        assertNotNull(response.getReviewedAt());
        verify(renewalRequestRepository).save(renewalRequest);
    }

    @Test
    void alreadyReviewedRequestCannotBeReviewedAgain() {
        authenticate(ADMIN_ID, Role.SYSTEM_ADMIN);

        UUID requestId = UUID.randomUUID();
        RenewalRequest renewalRequest = request(requestId, RenewalRequestStatus.APPROVED);
        when(renewalRequestRepository.findById(requestId)).thenReturn(Optional.of(renewalRequest));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> service.rejectRequest(requestId, new ReviewRenewalRequest()));

        assertEquals(ErrorCode.INVALID_RENEWAL_REQUEST_STATUS, exception.getErrorCode());
    }

    private void authenticate(UUID userId, Role role) {
        UserPrincipal principal = new UserPrincipal(userId, "current-user", "password", TENANT_ID, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private RenewalRequest request(UUID requestId, RenewalRequestStatus status) {
        RenewalRequest request = new RenewalRequest();
        request.setId(requestId);
        request.setTenantId(TENANT_ID);
        request.setRequestedBy(OWNER_ID);
        request.setPhoneNumber("01012345678");
        request.setAmount(new BigDecimal("500.00"));
        request.setPeriodMonths(3);
        request.setStatus(status);
        request.setCreatedAt(LocalDateTime.of(2026, 4, 25, 15, 0));
        request.setUpdatedAt(LocalDateTime.of(2026, 4, 25, 15, 5));
        return request;
    }

    private RenewalRequestReadProjection projection(UUID requestId,
                                                    RenewalRequestStatus status,
                                                    LocalDateTime createdAt) {
        return new RenewalRequestReadProjection() {
            @Override
            public UUID getId() {
                return requestId;
            }

            @Override
            public UUID getTenantId() {
                return TENANT_ID;
            }

            @Override
            public UUID getRequestedBy() {
                return OWNER_ID;
            }

            @Override
            public String getPhoneNumber() {
                return "01012345678";
            }

            @Override
            public BigDecimal getAmount() {
                return new BigDecimal("500.00");
            }

            @Override
            public Integer getPeriodMonths() {
                return 3;
            }

            @Override
            public RenewalRequestStatus getStatus() {
                return status;
            }

            @Override
            public LocalDateTime getReviewedAt() {
                return null;
            }

            @Override
            public UUID getReviewedBy() {
                return null;
            }

            @Override
            public String getAdminNote() {
                return null;
            }

            @Override
            public LocalDateTime getCreatedAt() {
                return createdAt;
            }

            @Override
            public LocalDateTime getUpdatedAt() {
                return createdAt.plusMinutes(5);
            }
        };
    }
}
