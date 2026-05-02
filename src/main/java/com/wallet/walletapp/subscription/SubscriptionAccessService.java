package com.wallet.walletapp.subscription;

import com.wallet.walletapp.branch.BranchRepository;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.plan.Plan;
import com.wallet.walletapp.plan.PlanService;
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
            throw new BusinessException(ErrorCode.FORBIDDEN, "Current plan is inactive");
        }
        if (subscription.getExpireDate().isBefore(LocalDate.now())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "Tenant subscription has expired");
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateUserLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (userRepository.countByTenantId(tenantId) >= plan.getMaxUsers()) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "User limit reached for current plan");
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateWalletLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (walletRepository.countByTenantId(tenantId) >= plan.getMaxWallets()) {
            throw new BusinessException(ErrorCode.WALLET_LIMIT_EXCEEDED);
        }
    }

    @Transactional(readOnly = true)
    public void validateCreateBranchLimit(UUID tenantId) {
        Plan plan = getCurrentPlan(tenantId);
        if (branchRepository.countByTenantId(tenantId) >= plan.getMaxBranches()) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "Branch limit reached for current plan");
        }
    }

    private Plan getCurrentPlan(UUID tenantId) {
        TenantSubscription subscription = tenantSubscriptionService.getCurrentSubscriptionEntity(tenantId);
        return planService.getPlanEntity(subscription.getPlanId());
    }
}
