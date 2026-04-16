package com.wallet.walletapp.plan;

import com.wallet.walletapp.plan.dto.PlanResponse;
import org.springframework.stereotype.Component;

@Component
public class PlanMapper {

    public PlanResponse toResponse(Plan plan) {
        PlanResponse response = new PlanResponse();
        response.setId(plan.getId());
        response.setName(plan.getName());
        response.setDescription(plan.getDescription());
        response.setMaxUsers(plan.getMaxUsers());
        response.setMaxWallets(plan.getMaxWallets());
        response.setMaxBranches(plan.getMaxBranches());
        response.setActive(plan.isActive());
        response.setCreatedAt(plan.getCreatedAt());
        response.setUpdatedAt(plan.getUpdatedAt());
        return response;
    }
}
