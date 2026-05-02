package com.wallet.walletapp.subscription;

import com.wallet.walletapp.plan.Plan;
import com.wallet.walletapp.plan.dto.TenantSubscriptionResponse;
import org.springframework.stereotype.Component;

@Component
public class TenantSubscriptionMapper {

    public TenantSubscriptionResponse toResponse(TenantSubscription subscription, Plan plan) {
        TenantSubscriptionResponse response = new TenantSubscriptionResponse();
        response.setId(subscription.getId());
        response.setTenantId(subscription.getTenantId());
        response.setPlanId(subscription.getPlanId());
        response.setPlanName(plan.getName());
        response.setStartDate(subscription.getStartDate());
        response.setExpireDate(subscription.getExpireDate());
        response.setValid(!subscription.getExpireDate().isBefore(java.time.LocalDate.now()));
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        return response;
    }
}
