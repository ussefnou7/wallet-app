package com.wallet.walletapp.wallet;

import com.wallet.walletapp.wallet.dto.WalletResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletUserRepository extends JpaRepository<WalletUser, UUID> {

    boolean existsByUserIdAndWalletIdAndTenantId(UUID userId, UUID walletId, UUID tenantId);

    List<WalletUser> findByUserIdAndTenantId(UUID userId, UUID tenantId);
}
