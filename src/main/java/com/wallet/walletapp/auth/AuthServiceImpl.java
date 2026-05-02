package com.wallet.walletapp.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.auth.dto.AuthResponse;
import com.wallet.walletapp.auth.dto.ChangePasswordRequest;
import com.wallet.walletapp.auth.dto.ForgotPasswordRequest;
import com.wallet.walletapp.auth.dto.LoginRequest;
import com.wallet.walletapp.common.dto.MessageResponse;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.exception.UnauthorizedException;
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
import com.wallet.walletapp.user.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final MessageResponse FORGOT_PASSWORD_RESPONSE =
            new MessageResponse("If the account exists, a reset request has been submitted.");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final SupportTicketRepository supportTicketRepository;
    private final NotificationCreationService notificationCreationService;
    private final TenantRepository tenantRepository;
    private final ObjectMapper objectMapper;

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

    @Override
    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByUsername(request.username())
                .ifPresentOrElse(this::processForgotPasswordRequest, () ->
                        log.info("Password reset requested for unknown username '{}'", request.username()));
        return FORGOT_PASSWORD_RESPONSE;
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request) {
        UserPrincipal principal = currentUser();
        if (principal.getRole() != Role.OWNER && principal.getRole() != Role.USER) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "Old password is invalid",
                    Map.of("oldPassword", "Old password is invalid")
            );
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        // TODO invalidate active tokens when token blacklist or refresh token support is added.
        return new MessageResponse("Password changed successfully");
    }

    private void processForgotPasswordRequest(User user) {
        try {
            switch (user.getRole()) {
                case OWNER -> createOwnerPasswordResetTicket(user);
                case USER -> createUserPasswordResetNotifications(user);
                default -> log.info("Password reset requested for unsupported role {} and username '{}'",
                        user.getRole(), user.getUsername());
            }
        } catch (Exception ex) {
            log.error("Failed to process password reset request for user {}", user.getId(), ex);
        }
    }

    private void createOwnerPasswordResetTicket(User owner) {
        if (owner.getTenantId() == null) {
            log.warn("Skipping password reset ticket for owner {} because tenantId is null", owner.getId());
            return;
        }

        SupportTicket ticket = SupportTicket.builder()
                .createdBy(owner.getId())
                .subject("Owner password reset request")
                .description(buildOwnerResetDescription(owner))
                .type(SupportTicketType.PASSWORD_RESET)
                .priority(SupportTicketPriority.HIGH)
                .status(SupportTicketStatus.OPEN)
                .build();
        ticket.setTenantId(owner.getTenantId());

        supportTicketRepository.save(ticket);
    }

    private void createUserPasswordResetNotifications(User user) {
        if (user.getTenantId() == null) {
            log.warn("Skipping password reset notification for user {} because tenantId is null", user.getId());
            return;
        }

        List<User> owners = userRepository.findByTenantIdAndRole(user.getTenantId(), Role.OWNER).stream()
                .filter(User::isActive)
                .filter(owner -> !owner.getId().equals(user.getId()))
                .toList();
        if (owners.isEmpty()) {
            return;
        }

        String message = "User %s requested password reset.".formatted(user.getUsername());
        String payloadJson = buildUserResetPayload(user);
        for (User owner : owners) {
            notificationCreationService.createIfNotExists(NotificationCreateCommand.builder()
                    .tenantId(user.getTenantId())
                    .recipientUserId(owner.getId())
                    .type(NotificationType.PASSWORD_RESET_REQUEST)
                    .priority(NotificationPriority.HIGH)
                    .severity(NotificationSeverity.HIGH)
                    .titleKey("Password reset request")
                    .messageKey(message)
                    .payloadJson(payloadJson)
                    .targetType("USER")
                    .targetId(user.getId())
                    .idempotencyKey(null)
                    .build());
        }
    }

    private String buildOwnerResetDescription(User owner) {
        StringBuilder description = new StringBuilder()
                .append("Owner username: ")
                .append(owner.getUsername());

        if (owner.getTenantId() != null) {
            description.append(System.lineSeparator())
                    .append("Tenant ID: ")
                    .append(owner.getTenantId());

            tenantRepository.findById(owner.getTenantId())
                    .map(Tenant::getName)
                    .ifPresent(name -> description.append(System.lineSeparator())
                            .append("Tenant name: ")
                            .append(name));
        }

        return description.toString();
    }

    private String buildUserResetPayload(User user) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("userId", user.getId());
        payload.put("username", user.getUsername());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize password reset notification payload", ex);
        }
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
