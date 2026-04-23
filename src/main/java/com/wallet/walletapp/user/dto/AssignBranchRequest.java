package com.wallet.walletapp.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AssignBranchRequest {

    @NotNull
    private UUID branchId;
}
