package com.wallet.walletapp.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ApiSecurityHandlersTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiErrorResponseFactory errorResponseFactory = new ApiErrorResponseFactory();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void authenticationEntryPointWritesUnauthorizedJson() throws Exception {
        ApiAuthenticationEntryPoint entryPoint = new ApiAuthenticationEntryPoint(objectMapper, errorResponseFactory);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured/resource");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new InsufficientAuthenticationException("Full authentication is required"));

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, response.getStatus());
        assertEquals("UNAUTHORIZED", json.get("code").asText());
        assertEquals("/secured/resource", json.get("path").asText());
        assertFalse(json.get("traceId").asText().isBlank());
    }

    @Test
    void accessDeniedHandlerWritesForbiddenJson() throws Exception {
        ApiAccessDeniedHandler handler = new ApiAccessDeniedHandler(objectMapper, errorResponseFactory);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/secured/admin");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("Denied"));

        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertEquals(403, response.getStatus());
        assertEquals("FORBIDDEN", json.get("code").asText());
        assertEquals("/secured/admin", json.get("path").asText());
        assertFalse(json.get("traceId").asText().isBlank());
    }
}
