package com.wallet.walletapp.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BranchUserRepository extends JpaRepository<BranchUser, UUID> {

    List<BranchUser> findAllByUserIdAndTenantId(UUID userId, UUID tenantId);

    void deleteAllByUserIdAndTenantId(UUID userId, UUID tenantId);
}
