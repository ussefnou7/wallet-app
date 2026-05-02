package com.wallet.walletapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank
        String oldPassword,
        @NotBlank
        @Size(min = 8)
        String newPassword
) {
}
