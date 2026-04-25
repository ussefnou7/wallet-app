package com.wallet.walletapp.tenant;

import com.wallet.walletapp.tenant.dto.TenantResponse;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public TenantResponse toResponse(Tenant tenant) {
        TenantResponse response = new TenantResponse();
        response.setId(tenant.getId());
        response.setName(tenant.getName());
        response.setPhoneNumber(tenant.getPhoneNumber());
        response.setActive(tenant.isActive());
        response.setCreatedAt(tenant.getCreatedAt());
        response.setUpdatedAt(tenant.getUpdatedAt());
        return response;
    }
}
