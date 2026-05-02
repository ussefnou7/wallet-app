package com.wallet.walletapp.auth;

import com.wallet.walletapp.auth.dto.AuthResponse;
import com.wallet.walletapp.auth.dto.ChangePasswordRequest;
import com.wallet.walletapp.auth.dto.ForgotPasswordRequest;
import com.wallet.walletapp.auth.dto.LoginRequest;
import com.wallet.walletapp.common.dto.MessageResponse;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    //@PreAuthorize("hasAnyRole('SYSTEM_ADMIN')")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PatchMapping("/me/password")
    @PreAuthorize("hasAnyRole('OWNER', 'USER')")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(request));
    }
}
