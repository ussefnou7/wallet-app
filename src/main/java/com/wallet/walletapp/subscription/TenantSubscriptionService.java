package com.wallet.walletapp.subscription;

import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.plan.Plan;
import com.wallet.walletapp.plan.PlanService;
import com.wallet.walletapp.plan.dto.AssignTenantSubscriptionRequest;
import com.wallet.walletapp.plan.dto.TenantSubscriptionResponse;
import com.wallet.walletapp.tenant.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantSubscriptionService {

    private final TenantSubscriptionRepository tenantSubscriptionRepository;
    private final TenantSubscriptionMapper tenantSubscriptionMapper;
    private final TenantRepository tenantRepository;
    private final PlanService planService;

    @Transactional
    public TenantSubscriptionResponse assignCurrentSubscription(AssignTenantSubscriptionRequest request) {
        tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND, "Tenant not found"));
        Plan plan = planService.getPlanEntity(request.getPlanId());

        TenantSubscription subscription = tenantSubscriptionRepository.findByTenantId(request.getTenantId())
                .orElseGet(TenantSubscription::new);

        subscription.setTenantId(request.getTenantId());
        subscription.setPlanId(plan.getId());
        subscription.setStartDate(request.getStartDate());
        subscription.setExpireDate(request.getExpireDate());

        subscription = tenantSubscriptionRepository.save(subscription);
        log.info("Tenant {} assigned to plan {}", request.getTenantId(), request.getPlanId());
        return tenantSubscriptionMapper.toResponse(subscription, plan);
    }

    @Transactional(readOnly = true)
    public TenantSubscriptionResponse getCurrentSubscription(UUID tenantId) {
        TenantSubscription subscription = getCurrentSubscriptionEntity(tenantId);
        Plan plan = planService.getPlanEntity(subscription.getPlanId());
        return tenantSubscriptionMapper.toResponse(subscription, plan);
    }

    @Transactional(readOnly = true)
    public TenantSubscription getCurrentSubscriptionEntity(UUID tenantId) {
        return tenantSubscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "Current tenant subscription not found"));
    }
}
