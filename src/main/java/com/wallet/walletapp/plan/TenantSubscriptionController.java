package com.wallet.walletapp.plan;

import com.wallet.walletapp.plan.dto.AssignTenantSubscriptionRequest;
import com.wallet.walletapp.plan.dto.TenantSubscriptionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenant-subscriptions")
@RequiredArgsConstructor
public class TenantSubscriptionController {

    private final TenantSubscriptionService tenantSubscriptionService;

    @PutMapping("/current")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantSubscriptionResponse> assignCurrentSubscription(
            @Valid @RequestBody AssignTenantSubscriptionRequest request) {
        return ResponseEntity.ok(tenantSubscriptionService.assignCurrentSubscription(request));
    }

    @GetMapping("/tenants/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantSubscriptionResponse> getCurrentSubscription(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(tenantSubscriptionService.getCurrentSubscription(tenantId));
    }
}
