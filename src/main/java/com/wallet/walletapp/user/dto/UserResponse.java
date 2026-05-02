package com.wallet.walletapp.user.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String username;
    private String role;
    private String tenantName;
    private boolean active;
    private String branchName;
}
