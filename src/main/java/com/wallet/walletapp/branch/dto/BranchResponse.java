package com.wallet.walletapp.branch.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class BranchResponse {
    private UUID branchId;
    private String name;
    private boolean active;
}
