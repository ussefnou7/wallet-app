package com.wallet.walletapp.user;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.branch.Branch;
import com.wallet.walletapp.branch.BranchRepository;
import com.wallet.walletapp.branch.BranchUser;
import com.wallet.walletapp.branch.BranchUserRepository;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.plan.SubscriptionAccessService;
import com.wallet.walletapp.tenant.TenantRepository;
import com.wallet.walletapp.user.dto.AssignBranchRequest;
import com.wallet.walletapp.user.dto.UserResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID OTHER_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private BranchUserRepository branchUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SubscriptionAccessService subscriptionAccessService;

    @InjectMocks
    private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void assignUserToBranchSavesTenantAwareAssignment() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.USER);
        Branch branch = branch(branchId, TENANT_ID);
        UserResponse response = new UserResponse();
        UserReadProjection projection = org.mockito.Mockito.mock(UserReadProjection.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(branchUserRepository.findAllByUserIdAndTenantId(userId, TENANT_ID)).thenReturn(List.of());
        when(userRepository.findReadById(userId)).thenReturn(Optional.of(projection));
        when(userMapper.toResponse(projection)).thenReturn(response);

        UserResponse result = userService.assignUserToBranch(userId, assignBranchRequest(branchId));

        assertSame(response, result);

        ArgumentCaptor<BranchUser> assignmentCaptor = ArgumentCaptor.forClass(BranchUser.class);
        verify(branchUserRepository).save(assignmentCaptor.capture());
        BranchUser assignment = assignmentCaptor.getValue();
        assertEquals(TENANT_ID, assignment.getTenantId());
        assertEquals(userId, assignment.getUserId());
        assertEquals(branchId, assignment.getBranchId());
    }

    @Test
    void assignUserToBranchIsIdempotentForSameBranch() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.USER);
        Branch branch = branch(branchId, TENANT_ID);
        BranchUser existingAssignment = branchUser(userId, branchId, TENANT_ID);
        UserResponse response = new UserResponse();
        UserReadProjection projection = org.mockito.Mockito.mock(UserReadProjection.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
        when(branchUserRepository.findAllByUserIdAndTenantId(userId, TENANT_ID)).thenReturn(List.of(existingAssignment));
        when(userRepository.findReadById(userId)).thenReturn(Optional.of(projection));
        when(userMapper.toResponse(projection)).thenReturn(response);

        UserResponse result = userService.assignUserToBranch(userId, assignBranchRequest(branchId));

        assertSame(response, result);
        verify(branchUserRepository, never()).save(any());
    }

    @Test
    void assignUserToBranchRejectsTenantMismatch() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.USER);
        Branch branch = branch(branchId, OTHER_TENANT_ID);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));

        assertThrows(BusinessValidationException.class,
                () -> userService.assignUserToBranch(userId, assignBranchRequest(branchId)));
        verify(branchUserRepository, never()).save(any());
    }

    @Test
    void assignUserToBranchRejectsNonUserRole() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.OWNER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BusinessValidationException.class,
                () -> userService.assignUserToBranch(userId, assignBranchRequest(branchId)));
        verifyNoInteractions(branchRepository);
        verify(branchUserRepository, never()).save(any());
    }

    @Test
    void assignUserToBranchThrowsNotFoundForMissingBranch() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(branchRepository.findById(branchId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> userService.assignUserToBranch(userId, assignBranchRequest(branchId)));
        verify(branchUserRepository, never()).save(any());
    }

    @Test
    void unassignUserFromBranchDeletesTenantScopedAssignments() {
        authenticate(Role.OWNER, TENANT_ID);

        UUID userId = UUID.randomUUID();
        User user = user(userId, TENANT_ID, Role.USER);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.unassignUserFromBranch(userId);

        verify(branchUserRepository).deleteAllByUserIdAndTenantId(userId, TENANT_ID);
    }

    private void authenticate(Role role, UUID tenantId) {
        UserPrincipal principal = new UserPrincipal(UUID.randomUUID(), "current-user", "password", tenantId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private AssignBranchRequest assignBranchRequest(UUID branchId) {
        AssignBranchRequest request = new AssignBranchRequest();
        request.setBranchId(branchId);
        return request;
    }

    private User user(UUID id, UUID tenantId, Role role) {
        User user = new User();
        user.setId(id);
        user.setTenantId(tenantId);
        user.setRole(role);
        user.setUsername("user-" + id);
        user.setPassword("password");
        return user;
    }

    private Branch branch(UUID id, UUID tenantId) {
        Branch branch = new Branch();
        branch.setId(id);
        branch.setTenantId(tenantId);
        branch.setName("branch-" + id);
        return branch;
    }

    private BranchUser branchUser(UUID userId, UUID branchId, UUID tenantId) {
        BranchUser branchUser = new BranchUser();
        branchUser.setUserId(userId);
        branchUser.setBranchId(branchId);
        branchUser.setTenantId(tenantId);
        return branchUser;
    }
}
