package com.wallet.walletapp.tenant;

import com.wallet.walletapp.tenant.dto.CreateTenantRequest;
import com.wallet.walletapp.tenant.dto.TenantResponse;
import com.wallet.walletapp.tenant.dto.UpdateTenantRequest;

import java.util.List;
import java.util.UUID;

public interface TenantService {

    TenantResponse createTenant(CreateTenantRequest request);

    List<TenantResponse> getAllTenants(Integer page, Integer size);

    TenantResponse getTenantById(UUID id);

    TenantResponse updateTenant(UUID id, UpdateTenantRequest request);

    void deleteTenant(UUID id);
}
