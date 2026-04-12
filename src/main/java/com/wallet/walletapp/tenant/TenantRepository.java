package com.wallet.walletapp.tenant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Page<Tenant> findAllByOrderByIdAsc(Pageable pageable);
}
