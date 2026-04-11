package com.wallet.walletapp.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Branch findByTenantIdAndId(UUID tenantId, UUID id);
    List<Branch> findByTenantId(UUID tenantId);
}
