package com.wallet.walletapp.wallet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    List<Wallet> findByTenantId(UUID tenantId);

    Optional<Wallet> findByIdAndTenantId(UUID id, UUID tenantId);

    List<Wallet> findByIdInAndTenantId(List<UUID> ids, UUID tenantId);

    List<Wallet> findByTenantIdAndType(UUID tenantId, WalletType type);

    List<Wallet> findByBranchId(UUID branchId);

    List<Wallet> findByBranchIdAndType(UUID branchId, WalletType type);
}
