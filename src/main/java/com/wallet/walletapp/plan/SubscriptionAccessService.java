package com.wallet.walletapp.plan;

import com.wallet.walletapp.branch.BranchRepository;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.wallet.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionAccessService {

    private final TenantSubscriptionService tenantSubscriptionService;
    private final PlanService planService;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public void validateValidSubscription(UUID tenantId) {
        TenantSubscription subscription = tenantSubscriptionService.getCurrentSubscriptionEntity(tenantId);
        Plan plan = planService.getPlanEntity(subscription.getPlanId());

        if (!plan.isActive()) {
            throw new IllegalStateException("Current plan is inactive");
        }
        if (subscription.getExpireDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Tenant subscription has expired");
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateUserLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (userRepository.countByTenantId(tenantId) >= plan.getMaxUsers()) {
            throw new IllegalStateException("User limit reached for current plan");
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateWalletLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (walletRepository.countByTenantId(tenantId) >= plan.getMaxWallets()) {
            throw new IllegalStateException("Wallet limit reached for current plan");
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateBranchLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (branchRepository.countByTenantId(tenantId) >= plan.getMaxBranches()) {
            throw new IllegalStateException("Branch limit reached for current plan");
        }
    }

    private Plan getCurrentPlan(UUID tenantId) {
        TenantSubscription subscription = tenantSubscriptionService.getCurrentSubscriptionEntity(tenantId);
        return planService.getPlanEntity(subscription.getPlanId());
    }
}
