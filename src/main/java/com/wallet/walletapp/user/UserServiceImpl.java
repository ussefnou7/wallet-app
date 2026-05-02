package com.wallet.walletapp.user;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.branch.Branch;
import com.wallet.walletapp.branch.BranchRepository;
import com.wallet.walletapp.branch.BranchUser;
import com.wallet.walletapp.branch.BranchUserRepository;
import com.wallet.walletapp.common.dto.MessageResponse;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.plan.SubscriptionAccessService;
import com.wallet.walletapp.tenant.TenantRepository;
import com.wallet.walletapp.user.dto.AssignBranchRequest;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import com.wallet.walletapp.user.dto.ResetUserPasswordRequest;
import com.wallet.walletapp.user.dto.UpdateUserRequest;
import com.wallet.walletapp.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final BranchRepository branchRepository;
    private final BranchUserRepository branchUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final SubscriptionAccessService subscriptionAccessService;


    @Override
    public void deleteUser(UUID userId) {
        UserPrincipal user = currentUser();
        if(user.getRole() == Role.SYSTEM_ADMIN) {
            userRepository.deleteById(userId);
        } else if(user.getRole() == Role.OWNER) {
            User userToDelete = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
            if (!Objects.equals(user.getTenantId(), userToDelete.getTenantId())) {
                throw new UnauthorizedException("Not authorized");
            }
        }
        else {
            throw new UnauthorizedException("Only system admin can delete users");
        }
    }

    @Override
    public List<UserResponse> getAllUsers(Integer page, Integer size) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        List<UserReadProjection> users;
        Pageable pageable = buildPageable(page, size);
        if (pageable != null) {
            if (user.getRole() == Role.SYSTEM_ADMIN) {
                users = userRepository.findAllForRead(pageable).getContent();
            } else {
                users = userRepository.findAllByTenantIdForRead(tenantId, pageable).getContent();
            }
        } else if (user.getRole() == Role.SYSTEM_ADMIN) {
            users = userRepository.findAllForRead();
        } else {
            users = userRepository.findAllByTenantIdForRead(tenantId);
        }

        return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<UserResponse> getAllOwners() {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        List<UserReadProjection> users;
        if (user.getRole() == Role.SYSTEM_ADMIN) {
            users = userRepository.findAllByRoleForRead(Role.OWNER);
        } else {
            users = userRepository.findAllByTenantIdAndRoleForRead(tenantId,Role.OWNER);
        }

        return users.stream().map(userMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(UUID userId, UpdateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        // Authorization: system admin can update anyone
        if (currentUser.getRole() != Role.SYSTEM_ADMIN) {
            if (currentUser.getRole() == Role.OWNER) {
                // Owner can only manage users within their tenant
                if (!currentUser.getTenantId().equals(user.getTenantId())) {
                    throw new UnauthorizedException("Not authorized");
                }
            } else {
                // Regular users can only update themselves
                if (!currentUser.getUserId().equals(user.getId())) {
                    throw new UnauthorizedException("Not authorized");
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
        return userMapper.toResponse(userRepository.findReadById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found after update")));
    }

    @Override
    @Transactional
    public UserResponse assignUserToBranch(UUID userId, AssignBranchRequest request) {
        if (request == null || request.getBranchId() == null) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "Branch id is required",
                    Map.of("branchId", "must not be null")
            );
        }

        UserPrincipal currentUser = currentUser();
        validateCanManageBranchAssignments(currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
        validateCanManageTenant(currentUser, user.getTenantId());
        validateBranchAssignableUser(user);

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BRANCH_NOT_FOUND, "Branch not found"));
        validateSameTenant(user, branch);

        List<BranchUser> assignments = branchUserRepository.findAllByUserIdAndTenantId(user.getId(), user.getTenantId());
        if (!assignments.isEmpty()) {
            boolean alreadyAssignedToRequestedBranch = assignments.size() == 1
                    && branch.getId().equals(assignments.getFirst().getBranchId());
            if (alreadyAssignedToRequestedBranch) {
                return findUserResponse(user.getId());
            }
            throw new BusinessValidationException(ErrorCode.DATA_CONFLICT, "User already assigned to a branch");
        }

        BranchUser assignment = new BranchUser();
        assignment.setTenantId(user.getTenantId());
        assignment.setUserId(user.getId());
        assignment.setBranchId(branch.getId());

        branchUserRepository.save(assignment);
        log.info("User {} assigned to branch {}", user.getId(), branch.getId());

        return findUserResponse(user.getId());
    }

    @Override
    @Transactional
    public void unassignUserFromBranch(UUID userId) {
        UserPrincipal currentUser = currentUser();
        validateCanManageBranchAssignments(currentUser);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));
        validateCanManageTenant(currentUser, user.getTenantId());
        validateBranchAssignableUser(user);

        branchUserRepository.deleteAllByUserIdAndTenantId(user.getId(), user.getTenantId());
        log.info("Branch assignment removed for user {}", user.getId());
    }

    @Override
    @Transactional
    public MessageResponse resetUserPassword(UUID userId, ResetUserPasswordRequest request) {
        UserPrincipal currentUser = currentUser();
        if (currentUser.getRole() != Role.OWNER) {
            throw new UnauthorizedException("Not authorized");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (user.getRole() != Role.USER) {
            throw new BusinessValidationException(ErrorCode.BAD_REQUEST, "Owner can reset password only for USER accounts");
        }

        validateCanManageTenant(currentUser, user.getTenantId());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        // TODO invalidate active tokens when token blacklist or refresh token support is added.
        return new MessageResponse("Password reset successfully");
    }

    public UserResponse createOwner(CreateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        // 🔒 Only Admin
        if (currentUser.getRole() != Role.SYSTEM_ADMIN) {
            throw new UnauthorizedException("Only admin can create owners");
        }

        // ✅ Must provide tenantId
        if (request.getTenantId() == null) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "TenantId is required",
                    Map.of("tenantId", "must not be null")
            );
        }

        // ✅ Validate tenant exists
        tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.TENANT_NOT_FOUND, "Tenant not found"));
        subscriptionAccessService.validateValidSubscription(request.getTenantId());
        subscriptionAccessService.validateCreateUserLimit(request.getTenantId());

        validateUser(request.getUsername());

        User owner = buildUser(request.getUsername(), request.getPassword(), Role.OWNER, request.getTenantId());

        owner = userRepository.save(owner);
        log.info("Owner '{}' created for tenant {}", owner.getUsername(), owner.getTenantId());
        return userMapper.toResponse(userRepository.findReadById(owner.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "Owner not found after create")));
    }

    public UserResponse createUser(CreateUserRequest request) {

        UserPrincipal currentUser = currentUser();

        // 🔒 Only Owner
        if (currentUser.getRole() != Role.OWNER) {
            throw new UnauthorizedException("Only owner can create users");
        }

        subscriptionAccessService.validateValidSubscription(currentUser.getTenantId());
        subscriptionAccessService.validateCreateUserLimit(currentUser.getTenantId());
        validateUser(request.getUsername());

        User user = buildUser(request.getUsername(), request.getPassword(), request.getRole() != null ? request.getRole() : Role.USER, currentUser.getTenantId());

        user = userRepository.save(user);
        log.info("User '{}' created for tenant {}", user.getUsername(), user.getTenantId());
        return userMapper.toResponse(userRepository.findReadById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found after create")));

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
            throw new BusinessValidationException(ErrorCode.DATA_CONFLICT, "Username already taken");
        }
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private UserResponse findUserResponse(UUID userId) {
        return userMapper.toResponse(userRepository.findReadById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found")));
    }

    private void validateCanManageBranchAssignments(UserPrincipal currentUser) {
        if (currentUser.getRole() != Role.OWNER && currentUser.getRole() != Role.SYSTEM_ADMIN) {
            throw new UnauthorizedException("Not authorized");
        }
    }

    private void validateCanManageTenant(UserPrincipal currentUser, UUID tenantId) {
        if (currentUser.getRole() == Role.SYSTEM_ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.OWNER && Objects.equals(currentUser.getTenantId(), tenantId)) {
            return;
        }
        throw new UnauthorizedException("Not authorized");
    }

    private void validateSameTenant(User user, Branch branch) {
        if (!Objects.equals(user.getTenantId(), branch.getTenantId())) {
            throw new BusinessValidationException(ErrorCode.BAD_REQUEST, "User and branch must belong to the same tenant");
        }
    }

    private void validateBranchAssignableUser(User user) {
        if (user.getRole() != Role.USER) {
            throw new BusinessValidationException(ErrorCode.BAD_REQUEST, "Only USER role can be assigned to a branch");
        }
    }

    private Pageable buildPageable(Integer page, Integer size) {
        if (page == null && size == null) {
            return null;
        }
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;
        if (resolvedPage < 0 || resolvedSize < 1) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "Invalid pagination parameters",
                    Map.of("page", resolvedPage, "size", resolvedSize)
            );
        }
        return PageRequest.of(resolvedPage, resolvedSize);
    }
}
