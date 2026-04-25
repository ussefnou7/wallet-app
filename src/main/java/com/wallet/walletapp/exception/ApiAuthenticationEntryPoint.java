package com.wallet.walletapp.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final ApiErrorResponseFactory errorResponseFactory;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String message = authException instanceof BadCredentialsException
                ? "Invalid username or password"
                : ErrorCode.UNAUTHORIZED.getDefaultMessage();
        ApiErrorResponse apiError = errorResponseFactory.create(request, ErrorCode.UNAUTHORIZED, message, Map.of());

        log.warn("Authentication failure on {} [{}]: {}", request.getRequestURI(), apiError.getTraceId(), authException.getMessage());

        response.setStatus(apiError.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
