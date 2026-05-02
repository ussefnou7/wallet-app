package com.wallet.walletapp.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ForgotPasswordRequest(
        @NotBlank
        @Size(min = 3, max = 255)
        String username
) {

    public ForgotPasswordRequest {
        username = username == null ? null : username.trim();
    }
}
