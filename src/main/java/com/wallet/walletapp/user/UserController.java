package com.wallet.walletapp.user;

import com.wallet.walletapp.user.dto.AssignBranchRequest;
import com.wallet.walletapp.user.dto.CreateUserRequest;
import com.wallet.walletapp.user.dto.UpdateUserRequest;
import com.wallet.walletapp.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<UserResponse> create(@Validated @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createUser(request));
    }

    @PostMapping("/create-owner")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<UserResponse> createOwner(@Validated @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.createOwner(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable UUID id,
            @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @PutMapping("/{userId}/assign-branch")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<UserResponse> assignBranch(
            @PathVariable UUID userId,
            @Valid @RequestBody AssignBranchRequest request) {
        return ResponseEntity.ok(userService.assignUserToBranch(userId, request));
    }

    @DeleteMapping("/{userId}/branch")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> unassignBranch(@PathVariable UUID userId) {
        userService.unassignUserFromBranch(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @GetMapping("/owners")
    public ResponseEntity<List<UserResponse>> getAllOwners() {
        return ResponseEntity.ok(userService.getAllOwners());
    }
}
