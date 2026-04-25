package com.wallet.walletapp.renewal;

import com.wallet.walletapp.renewal.dto.CreateRenewalRequest;
import com.wallet.walletapp.renewal.dto.RenewalRequestResponse;
import com.wallet.walletapp.renewal.dto.ReviewRenewalRequest;
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
public class RenewalRequestController {

    private final RenewalRequestService renewalRequestService;

    @PostMapping("/api/v1/renewal-requests")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RenewalRequestResponse> createRequest(@Valid @RequestBody CreateRenewalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(renewalRequestService.createRequest(request));
    }

    @GetMapping("/api/v1/renewal-requests/my")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<RenewalRequestResponse>> getMyRequests() {
        return ResponseEntity.ok(renewalRequestService.getMyRequests());
    }

    @GetMapping("/api/v1/renewal-requests/{requestId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<RenewalRequestResponse> getMyRequest(@PathVariable UUID requestId) {
        return ResponseEntity.ok(renewalRequestService.getMyRequest(requestId));
    }

    @GetMapping("/api/v1/admin/renewal-requests")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<List<RenewalRequestResponse>> getAdminRequests(
            @RequestParam(required = false) RenewalRequestStatus status,
            @RequestParam(required = false) UUID tenantId) {
        return ResponseEntity.ok(renewalRequestService.getAdminRequests(status, tenantId));
    }

    @PatchMapping("/api/v1/admin/renewal-requests/{requestId}/approve")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RenewalRequestResponse> approveRequest(
            @PathVariable UUID requestId,
            @RequestBody(required = false) ReviewRenewalRequest request) {
        return ResponseEntity.ok(renewalRequestService.approveRequest(requestId, request));
    }

    @PatchMapping("/api/v1/admin/renewal-requests/{requestId}/reject")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<RenewalRequestResponse> rejectRequest(
            @PathVariable UUID requestId,
            @RequestBody(required = false) ReviewRenewalRequest request) {
        return ResponseEntity.ok(renewalRequestService.rejectRequest(requestId, request));
    }
}
