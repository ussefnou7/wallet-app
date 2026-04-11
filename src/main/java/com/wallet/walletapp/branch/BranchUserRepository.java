package com.wallet.walletapp.branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BranchUserRepository extends JpaRepository<BranchUser, UUID> {

    boolean existsByUserId(UUID userId);
    boolean existsByUserIdAndBranchId(UUID userId, UUID branchId);
}
