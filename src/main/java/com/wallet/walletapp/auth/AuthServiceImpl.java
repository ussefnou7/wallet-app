package com.wallet.walletapp.auth;

import com.wallet.walletapp.auth.dto.AuthResponse;
import com.wallet.walletapp.auth.dto.LoginRequest;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        if (!user.isActive()) {
            throw new UnauthorizedException(ErrorCode.FORBIDDEN, "User account is inactive");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTenantId(), user.getRole().name());

        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    @Override
    @Transactional
    public AuthResponse register(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DATA_CONFLICT, "Username already taken");
        }

        UUID tenantId = request.getTenantId() != null ? request.getTenantId() : UUID.randomUUID();

        User user = new User();
        user.setTenantId(tenantId);
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.USER);

        user = userRepository.save(user);
        log.info("User '{}' registered with role {}", user.getUsername(), user.getRole());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getTenantId(), user.getRole().name());
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }
}
