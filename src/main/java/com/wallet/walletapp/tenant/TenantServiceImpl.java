package com.wallet.walletapp.tenant;

import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.tenant.dto.CreateTenantRequest;
import com.wallet.walletapp.tenant.dto.TenantResponse;
import com.wallet.walletapp.tenant.dto.UpdateTenantRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantServiceImpl implements TenantService {

    private final TenantRepository tenantRepository;
    private final TenantMapper tenantMapper;

    @Override
    @Transactional
    public TenantResponse createTenant(CreateTenantRequest request) {
        validateNameAvailability(request.getName());

        Tenant tenant = new Tenant();
        tenant.setName(request.getName());
        tenant.setActive(true);

        tenant = tenantRepository.save(tenant);
        log.info("Tenant '{}' created", tenant.getName());
        return tenantMapper.toResponse(tenant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TenantResponse> getAllTenants() {
        return tenantRepository.findAll()
                .stream()
                .map(tenantMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TenantResponse getTenantById(UUID id) {
        return tenantMapper.toResponse(findTenantById(id));
    }

    @Override
    @Transactional
    public TenantResponse updateTenant(UUID id, UpdateTenantRequest request) {
        Tenant tenant = findTenantById(id);
        String requestedName = request.getName();

        if (!tenant.getName().equalsIgnoreCase(requestedName)) {
            validateNameAvailability(requestedName);
        }

        tenant.setName(requestedName);
        tenant.setActive(request.isActive());
        tenant = tenantRepository.save(tenant);
        log.info("Tenant {} updated", id);
        return tenantMapper.toResponse(tenant);
    }

    @Override
    @Transactional
    public void deleteTenant(UUID id) {
        Tenant tenant = findTenantById(id);
        tenantRepository.delete(tenant);
        log.info("Tenant {} deleted", id);
    }

    private Tenant findTenantById(UUID id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
    }

    private void validateNameAvailability(String name) {
        if (tenantRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Tenant name already exists");
        }
    }

}
