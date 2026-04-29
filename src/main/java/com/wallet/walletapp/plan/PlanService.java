package com.wallet.walletapp.plan;

import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.plan.dto.CreatePlanRequest;
import com.wallet.walletapp.plan.dto.PlanResponse;
import com.wallet.walletapp.plan.dto.UpdatePlanRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanMapper planMapper;

    @Transactional
    public PlanResponse createPlan(CreatePlanRequest request) {
        Plan plan = Plan.builder()
                .name(request.getName().trim())
                .price(request.getPrice())
                .description(request.getDescription())
                .maxUsers(request.getMaxUsers())
                .maxWallets(request.getMaxWallets())
                .maxBranches(request.getMaxBranches())
                .active(request.isActive())
                .build();

        plan = planRepository.save(plan);
        log.info("Plan '{}' created", plan.getName());
        return planMapper.toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getAllPlans() {
        return planRepository.findAllByOrderByNameAsc().stream()
                .map(planMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlanResponse getPlanById(UUID id) {
        return planMapper.toResponse(getPlanEntity(id));
    }

    @Transactional
    public PlanResponse updatePlan(UUID id, UpdatePlanRequest request) {
        Plan plan = getPlanEntity(id);
        plan.setName(request.getName().trim());
        plan.setDescription(request.getDescription());
        plan.setMaxUsers(request.getMaxUsers());
        plan.setMaxWallets(request.getMaxWallets());
        plan.setMaxBranches(request.getMaxBranches());
        plan.setActive(request.isActive());

        plan = planRepository.save(plan);
        log.info("Plan {} updated", id);
        return planMapper.toResponse(plan);
    }

    @Transactional
    public void deletePlan(UUID id) {
        Plan plan = getPlanEntity(id);
        planRepository.delete(plan);
        log.info("Plan {} deleted", id);
    }

    @Transactional(readOnly = true)
    public Plan getPlanEntity(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.ENTITY_NOT_FOUND, "Plan not found"));
    }
}
