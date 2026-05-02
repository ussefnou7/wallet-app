package com.wallet.walletapp.user;

import com.wallet.walletapp.common.dto.MessageResponse;
import com.wallet.walletapp.user.dto.AssignBranchRequest;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import com.wallet.walletapp.user.dto.ResetUserPasswordRequest;
import com.wallet.walletapp.user.dto.UpdateUserRequest;
import com.wallet.walletapp.user.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    UserResponse assignUserToBranch(UUID userId, AssignBranchRequest request);

    void unassignUserFromBranch(UUID userId);

    MessageResponse resetUserPassword(UUID userId, ResetUserPasswordRequest request);

    void deleteUser(UUID userId);

    List<UserResponse> getAllUsers(Integer page, Integer size);
    List<UserResponse> getAllOwners();

    UserResponse createOwner(CreateUserRequest request);

    void validateUser(String username);

    User buildUser(String username, String password, Role role, UUID tenantId);
}
