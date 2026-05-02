package com.wallet.walletapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetUserPasswordRequest(
        @NotBlank
        @Size(min = 6)
        String newPassword
) {
}
