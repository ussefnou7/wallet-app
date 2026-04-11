package com.wallet.walletapp.branch;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.branch.dto.CreateBranchRequest;
import com.wallet.walletapp.branch.dto.UpdateBranchRequest;
import com.wallet.walletapp.branch.dto.BranchResponse;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

    private final BranchRepository branchRepository;
    private final BranchMapper branchMapper;
    private final BranchUserRepository branchUserRepository;
    private final UserRepository userRepository;


    @Transactional
    public BranchResponse createBranch(CreateBranchRequest request) {

        Branch branch = new Branch();
        branch.setTenantId(request.getTenantId());
        branch.setName(request.getName());

        branch = branchRepository.save(branch);
        log.info("Branch '{}' created for tenant {}", branch.getName(), branch.getTenantId());
        return branchMapper.toResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, UpdateBranchRequest request) {
        Branch branch = branchRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Branch not found"));
        branch.setName(request.getName());
        branch = branchRepository.save(branch);
        log.info("Branch {} updated", id);
        return branchMapper.toResponse(branch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        List<Branch> branches = branchRepository.findAll();
        return branches.stream().map(branchMapper::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteBranch(UUID id) {
        branchRepository.deleteById(id);
    }
    public void assignUserToBranch( UUID tenantId, UUID userId, UUID branchId) {

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // 🔐 Tenant isolation
        if (!user.getTenantId().equals(tenantId) ||
                !branch.getTenantId().equals(tenantId)) {
            throw new RuntimeException("Cross-tenant access denied");
        }

        // ⚠️ Restriction for normal users
        if (user.getRole() == Role.USER) {
            boolean alreadyAssigned = branchUserRepository.existsByUserId(userId);
            if (alreadyAssigned) {
                throw new RuntimeException("User already assigned to a branch");
            }
        }

        // Prevent duplicates
        if (branchUserRepository.existsByUserIdAndBranchId(userId, branchId)) {
            throw new RuntimeException("Already assigned");
        }

        BranchUser bu = new BranchUser();
        bu.setUserId(userId);
        bu.setBranchId(branchId);
        bu.setTenantId(tenantId);

        branchUserRepository.save(bu);
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
