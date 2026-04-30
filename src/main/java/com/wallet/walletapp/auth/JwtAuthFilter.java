package com.wallet.walletapp.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.common.TenantContext;
import com.wallet.walletapp.exception.ApiErrorResponse;
import com.wallet.walletapp.exception.ApiErrorResponseFactory;
import com.wallet.walletapp.exception.ErrorCode;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ApiErrorResponseFactory errorResponseFactory;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims;
            try {
                claims = jwtUtil.extractClaims(token);
            } catch (ExpiredJwtException ex) {
                writeErrorResponse(request, response, ErrorCode.TOKEN_EXPIRED);
                return;
            } catch (JwtException | IllegalArgumentException ex) {
                writeErrorResponse(request, response, ErrorCode.INVALID_TOKEN);
                return;
            }

            UUID userId = extractUserId(claims);
            String username = extractUsername(claims);
            if (userId == null || username == null) {
                writeErrorResponse(request, response, ErrorCode.INVALID_TOKEN);
                return;
            }

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                writeErrorResponse(request, response, ErrorCode.INVALID_TOKEN);
                return;
            }
            if (!username.equals(user.getUsername())) {
                writeErrorResponse(request, response, ErrorCode.INVALID_TOKEN);
                return;
            }
            if (!user.isActive()) {
                writeErrorResponse(request, response, ErrorCode.ACCOUNT_INACTIVE);
                return;
            }

            UUID tenantId = user.getTenantId();
            Role role = user.getRole();
            TenantContext.setTenantId(tenantId);

            UserPrincipal principal = new UserPrincipal(userId, username, null, tenantId, role);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            SecurityContextHolder.clearContext();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private UUID extractUserId(Claims claims) {
        String rawUserId = claims.get("userId", String.class);
        if (!StringUtils.hasText(rawUserId)) {
            return null;
        }

        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String extractUsername(Claims claims) {
        String username = claims.getSubject();
        return StringUtils.hasText(username) ? username : null;
    }

    private void writeErrorResponse(HttpServletRequest request,
                                    HttpServletResponse response,
                                    ErrorCode errorCode) throws IOException {
        ApiErrorResponse apiError = errorResponseFactory.create(
                request,
                errorCode,
                errorCode.getDefaultMessage(),
                Map.of()
        );

        log.warn("JWT authentication failure {} on {} [{}]", apiError.getCode(), request.getRequestURI(), apiError.getTraceId());

        response.setStatus(apiError.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
