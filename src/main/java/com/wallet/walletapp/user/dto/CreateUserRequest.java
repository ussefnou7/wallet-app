package com.wallet.walletapp.user.dto;

import com.wallet.walletapp.user.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6)
    private String password;

    private Role role;

    /** Optional for creating User, Mandatory for creating Owner */
    private UUID tenantId;

}
