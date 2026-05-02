package com.wallet.walletapp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.auth.dto.ChangePasswordRequest;
import com.wallet.walletapp.auth.dto.ForgotPasswordRequest;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.notification.NotificationCreateCommand;
import com.wallet.walletapp.notification.NotificationCreationService;
import com.wallet.walletapp.notification.NotificationPriority;
import com.wallet.walletapp.notification.NotificationSeverity;
import com.wallet.walletapp.notification.NotificationType;
import com.wallet.walletapp.support.SupportTicket;
import com.wallet.walletapp.support.SupportTicketPriority;
import com.wallet.walletapp.support.SupportTicketRepository;
import com.wallet.walletapp.support.SupportTicketStatus;
import com.wallet.walletapp.support.SupportTicketType;
import com.wallet.walletapp.tenant.Tenant;
import com.wallet.walletapp.tenant.TenantRepository;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private NotificationCreationService notificationCreationService;

    @Mock
    private TenantRepository tenantRepository;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository,
                passwordEncoder,
                jwtUtil,
                supportTicketRepository,
                notificationCreationService,
                tenantRepository,
                new ObjectMapper()
        );
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void forgotPasswordUnknownUsernameReturnsGenericSuccess() {
        when(userRepository.findByUsername("missing-user")).thenReturn(Optional.empty());

        var response = authService.forgotPassword(new ForgotPasswordRequest("  missing-user  "));

        assertEquals("If the account exists, a reset request has been submitted.", response.message());
        verifyNoInteractions(supportTicketRepository, notificationCreationService, tenantRepository);
    }

    @Test
    void forgotPasswordOwnerCreatesHighPrioritySupportTicket() {
        UUID ownerId = UUID.randomUUID();
        User owner = user(ownerId, TENANT_ID, Role.OWNER, "owner-one");
        Tenant tenant = Tenant.builder().name("Tenant One").build();

        when(userRepository.findByUsername("owner-one")).thenReturn(Optional.of(owner));
        when(tenantRepository.findById(TENANT_ID)).thenReturn(Optional.of(tenant));

        var response = authService.forgotPassword(new ForgotPasswordRequest(" owner-one "));

        assertEquals("If the account exists, a reset request has been submitted.", response.message());

        ArgumentCaptor<SupportTicket> ticketCaptor = ArgumentCaptor.forClass(SupportTicket.class);
        verify(supportTicketRepository).save(ticketCaptor.capture());
        SupportTicket ticket = ticketCaptor.getValue();
        assertEquals(TENANT_ID, ticket.getTenantId());
        assertEquals(ownerId, ticket.getCreatedBy());
        assertEquals("Owner password reset request", ticket.getSubject());
        assertEquals(SupportTicketType.PASSWORD_RESET, ticket.getType());
        assertEquals(SupportTicketPriority.HIGH, ticket.getPriority());
        assertEquals(SupportTicketStatus.OPEN, ticket.getStatus());
        assertTrue(ticket.getDescription().contains("owner-one"));
        assertTrue(ticket.getDescription().contains("Tenant One"));
        verifyNoInteractions(notificationCreationService);
    }

    @Test
    void forgotPasswordUserCreatesHighSeverityOwnerNotification() {
        UUID userId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID inactiveOwnerId = UUID.randomUUID();

        User user = user(userId, TENANT_ID, Role.USER, "user-one");
        User owner = user(ownerId, TENANT_ID, Role.OWNER, "owner-one");
        User inactiveOwner = user(inactiveOwnerId, TENANT_ID, Role.OWNER, "owner-two");
        inactiveOwner.setActive(false);

        when(userRepository.findByUsername("user-one")).thenReturn(Optional.of(user));
        when(userRepository.findByTenantIdAndRole(TENANT_ID, Role.OWNER)).thenReturn(List.of(owner, inactiveOwner));

        var response = authService.forgotPassword(new ForgotPasswordRequest("user-one"));

        assertEquals("If the account exists, a reset request has been submitted.", response.message());

        ArgumentCaptor<NotificationCreateCommand> commandCaptor = ArgumentCaptor.forClass(NotificationCreateCommand.class);
        verify(notificationCreationService).createIfNotExists(commandCaptor.capture());
        NotificationCreateCommand command = commandCaptor.getValue();
        assertEquals(TENANT_ID, command.getTenantId());
        assertEquals(ownerId, command.getRecipientUserId());
        assertEquals(NotificationType.PASSWORD_RESET_REQUEST, command.getType());
        assertEquals(NotificationPriority.HIGH, command.getPriority());
        assertEquals(NotificationSeverity.HIGH, command.getSeverity());
        assertEquals("User user-one requested password reset.", command.getMessageKey());
        assertEquals(userId, command.getTargetId());
        assertTrue(command.getPayloadJson().contains(userId.toString()));
        verifyNoInteractions(supportTicketRepository);
    }

    @Test
    void changePasswordRejectsInvalidOldPassword() {
        UUID currentUserId = authenticate(Role.USER, TENANT_ID);
        User user = user(currentUserId, TENANT_ID, Role.USER, "user-one");
        user.setPassword("encoded-old");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass123", "encoded-old")).thenReturn(false);

        BusinessValidationException ex = assertThrows(
                BusinessValidationException.class,
                () -> authService.changePassword(new ChangePasswordRequest("wrongPass123", "newPass123"))
        );

        assertEquals("Old password is invalid", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordUpdatesEncodedPassword() {
        UUID currentUserId = authenticate(Role.OWNER, TENANT_ID);
        User user = user(currentUserId, TENANT_ID, Role.OWNER, "owner-one");
        user.setPassword("encoded-old");

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass123", "encoded-old")).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded-new");

        var response = authService.changePassword(new ChangePasswordRequest("oldPass123", "newPass123"));

        assertEquals("Password changed successfully", response.message());
        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    private UUID authenticate(Role role, UUID tenantId) {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "current-user", "password", tenantId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
        return userId;
    }

    private User user(UUID id, UUID tenantId, Role role, String username) {
        User user = new User();
        user.setId(id);
        user.setTenantId(tenantId);
        user.setRole(role);
        user.setUsername(username);
        user.setPassword("password");
        user.setActive(true);
        return user;
    }
}
