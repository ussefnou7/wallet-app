package com.wallet.walletapp.branch.dto;

import lombok.Data;

@Data
public class UpdateBranchRequest {
    private String name;
    private boolean active;
}
