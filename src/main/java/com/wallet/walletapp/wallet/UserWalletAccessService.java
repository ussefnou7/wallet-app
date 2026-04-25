package com.wallet.walletapp.wallet;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.branch.BranchUser;
import com.wallet.walletapp.branch.BranchUserRepository;
import com.wallet.walletapp.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserWalletAccessService {

    private final BranchUserRepository branchUserRepository;
    private final WalletRepository walletRepository;

    @Transactional(readOnly = true)
    public boolean hasAccessToWallet(UserPrincipal user, UUID walletId) {
        if (user.getRole() != Role.USER) {
            return true;
        }

        List<UUID> branchIds = findAccessibleBranchIds(user);
        if (branchIds.isEmpty()) {
            return false;
        }

        return walletRepository.existsByIdAndTenantIdAndBranchIdIn(walletId, user.getTenantId(), branchIds);
    }

    @Transactional(readOnly = true)
    public List<UUID> getAccessibleWalletIds(UserPrincipal user) {
        if (user.getRole() != Role.USER) {
            return List.of();
        }

        List<UUID> branchIds = findAccessibleBranchIds(user);
        if (branchIds.isEmpty()) {
            return List.of();
        }

        return walletRepository.findIdsByTenantIdAndBranchIdIn(user.getTenantId(), branchIds);
    }

    private List<UUID> findAccessibleBranchIds(UserPrincipal user) {
        return branchUserRepository.findAllByUserIdAndTenantId(user.getUserId(), user.getTenantId()).stream()
                .map(BranchUser::getBranchId)
                .distinct()
                .toList();
    }
}
