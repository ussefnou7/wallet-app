package com.wallet.walletapp.branch;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.branch.dto.CreateBranchRequest;
import com.wallet.walletapp.branch.dto.UpdateBranchRequest;
import com.wallet.walletapp.branch.dto.BranchResponse;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.plan.SubscriptionAccessService;
import com.wallet.walletapp.user.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final SubscriptionAccessService subscriptionAccessService;


    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {
        UserPrincipal user = currentUser();
        if (user.getRole() == Role.SYSTEM_ADMIN && request.getTenantId() == null) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "TenantId is required",
                    Map.of("tenantId", "must not be null")
            );
        }
        UUID tenantId = user.getRole() == Role.SYSTEM_ADMIN ? request.getTenantId() : user.getTenantId();
        subscriptionAccessService.validateValidSubscription(tenantId);
        subscriptionAccessService.validateCreateBranchLimit(tenantId);

        Branch branch = new Branch();
        branch.setTenantId(tenantId);
        branch.setName(request.getName());

        branch = branchRepository.save(branch);
        log.info("Branch '{}' created for tenant {}", branch.getName(), branch.getTenantId());
        return branchMapper.toResponse(branchRepository.findReadById(branch.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found after create")));
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found"));
        branch.setName(request.getName());
        branch = branchRepository.save(branch);
        log.info("Branch {} updated", id);
        return branchMapper.toResponse(branchRepository.findReadById(branch.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found after update")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches(Integer page, Integer size) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        Pageable pageable = buildPageable(page, size);
        List<BranchReadProjection> branches;
        if (pageable != null) {
            if (user.getRole() == Role.SYSTEM_ADMIN) {
                branches = branchRepository.findAllForRead(pageable).getContent();
            } else {
                branches = branchRepository.findAllByTenantIdForRead(tenantId, pageable).getContent();
            }
        } else if (user.getRole() == Role.SYSTEM_ADMIN) {
            branches = branchRepository.findAllForRead();
        } else {
            branches = branchRepository.findAllByTenantIdForRead(tenantId);
        }

        return branches.stream().map(branchMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteBranch(UUID id) {
        branchRepository.deleteById(id);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Pageable buildPageable(Integer page, Integer size) {
        if (page == null && size == null) {
            return null;
        }
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;
        if (resolvedPage < 0 || resolvedSize < 1) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "Invalid pagination parameters",
                    Map.of("page", resolvedPage, "size", resolvedSize)
            );
        }
        return PageRequest.of(resolvedPage, resolvedSize);
    }

}
