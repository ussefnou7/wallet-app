package com.wallet.walletapp.user;

import com.wallet.walletapp.user.dto.AssignBranchRequest;
import com.wallet.walletapp.user.dto.ChangePasswordRequest;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import com.wallet.walletapp.user.dto.UpdateUserRequest;
import com.wallet.walletapp.user.dto.UserResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID userId, UpdateUserRequest request);

    Map<String, String> changePassword(ChangePasswordRequest request);

    UserResponse assignUserToBranch(UUID userId, AssignBranchRequest request);

    void unassignUserFromBranch(UUID userId);

    void deleteUser(UUID userId);

    List<UserResponse> getAllUsers(Integer page, Integer size);
    List<UserResponse> getAllOwners();

    UserResponse createOwner(CreateUserRequest request);

    void validateUser(String username);

    User buildUser(String username, String password, Role role, UUID tenantId);
}
