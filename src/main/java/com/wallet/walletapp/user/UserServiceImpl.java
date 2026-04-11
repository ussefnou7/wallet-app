package com.wallet.walletapp.user;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.tenant.TenantRepository;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import com.wallet.walletapp.user.dto.UpdateUserRequest;
import com.wallet.walletapp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;


    @Override
    public void deleteUser(UUID userId) {

    }

    @Override
    public List<UserResponse> getAllUsers() {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        List<User> users;
        if (user.getRole() == Role.SYSTEM_ADMIN) {
            users = userRepository.findAll();
        } else {
            users = userRepository.findByTenantId(tenantId);
        }

        return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // Authorization: system admin can update anyone
        if (currentUser.getRole() != Role.SYSTEM_ADMIN) {
            if (currentUser.getRole() == Role.OWNER) {
                // Owner can only manage users within their tenant
                if (!currentUser.getTenantId().equals(user.getTenantId())) {
                    throw new RuntimeException("Not authorized");
                }
            } else {
                // Regular users can only update themselves
                if (!currentUser.getUserId().equals(user.getId())) {
                    throw new RuntimeException("Not authorized");
                }
            }
        }

        // If username changed, validate uniqueness
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            validateUser(request.getUsername());
            user.setUsername(request.getUsername());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setActive(request.isActive());

        user = userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public UserResponse createOwner(CreateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        // 🔒 Only Admin
        if (currentUser.getRole() != Role.SYSTEM_ADMIN) {
            throw new RuntimeException("Only admin can create owners");
        }

        // ✅ Must provide tenantId
        if (request.getTenantId() == null) {
            throw new RuntimeException("TenantId is required");
        }

        // ✅ Validate tenant exists
        tenantRepository.findById(request.getTenantId()).orElseThrow(() -> new RuntimeException("Tenant not found"));

        validateUser(request.getUsername());

        User owner = buildUser(request.getUsername(), request.getPassword(), Role.OWNER, request.getTenantId());

        owner = userRepository.save(owner);
        log.info("Owner '{}' created for tenant {}", owner.getUsername(), owner.getTenantId());
        return userMapper.toResponse(owner);
    }

    public UserResponse createUser(CreateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        // 🔒 Only Owner
        if (currentUser.getRole() != Role.OWNER) {
            throw new RuntimeException("Only owner can create users");
        }

        // ❌ Owner cannot pass tenantId
        /*
        if (request.getTenantId() != null) {
            throw new RuntimeException("TenantId should not be provided");
        }
         */

        validateUser(request.getUsername());

        User user = buildUser(request.getUsername(), request.getPassword(), request.getRole() != null ? request.getRole() : Role.USER, currentUser.getTenantId());

        user = userRepository.save(user);
        log.info("User '{}' created for tenant {}", user.getUsername(), user.getTenantId());
        return userMapper.toResponse(user);

    }


    public User buildUser(String username, String password, Role role, UUID tenantId) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setTenantId(tenantId);
        user.setActive(true);
        return user;
    }

    public void validateUser(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already taken");
        }
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
