package com.wallet.walletapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;

    private boolean active;
}
