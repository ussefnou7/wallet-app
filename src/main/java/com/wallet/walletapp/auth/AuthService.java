package com.wallet.walletapp.auth;

import com.wallet.walletapp.auth.dto.AuthResponse;
import com.wallet.walletapp.auth.dto.LoginRequest;
import com.wallet.walletapp.user.dto.CreateUserRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(CreateUserRequest request);
}
