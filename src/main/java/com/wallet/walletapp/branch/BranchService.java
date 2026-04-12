package com.wallet.walletapp.branch;

import com.wallet.walletapp.branch.dto.BranchResponse;
import com.wallet.walletapp.branch.dto.CreateBranchRequest;
import com.wallet.walletapp.branch.dto.UpdateBranchRequest;

import java.util.List;
import java.util.UUID;

public interface BranchService {
    BranchResponse createBranch(CreateBranchRequest request);
    BranchResponse updateBranch(UUID id, UpdateBranchRequest request);
    void deleteBranch(UUID id);
    List<BranchResponse> getAllBranches(Integer page, Integer size);

}
