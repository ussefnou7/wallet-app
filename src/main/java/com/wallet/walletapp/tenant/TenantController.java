package com.wallet.walletapp.tenant;

import com.wallet.walletapp.tenant.dto.CreateTenantRequest;
import com.wallet.walletapp.tenant.dto.TenantResponse;
import com.wallet.walletapp.tenant.dto.UpdateTenantRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenant(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<List<TenantResponse>> getAllTenants(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(tenantService.getAllTenants(page, size));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<TenantResponse> updateTenant(@PathVariable UUID id,
                                                       @Valid @RequestBody UpdateTenantRequest request) {
        return ResponseEntity.ok(tenantService.updateTenant(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}
