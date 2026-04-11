package com.wallet.walletapp.branch;

import com.wallet.walletapp.branch.dto.BranchResponse;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchResponse toResponse(Branch branch) {
        BranchResponse response = new BranchResponse();
        response.setBranchId(branch.getId());
        response.setActive(branch.isActive());
        response.setName(branch.getName());
        return response;
    }

}
