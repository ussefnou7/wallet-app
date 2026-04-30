package com.wallet.walletapp.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.common.TenantContext;
import com.wallet.walletapp.exception.ApiErrorResponseFactory;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.user.User;
import com.wallet.walletapp.user.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final String SECRET = "01234567890123456789012345678901";
    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000010");
    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @Mock
    private UserRepository userRepository;

    private ObjectMapper objectMapper;
    private JwtUtil jwtUtil;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3_600_000L);
        filter = new JwtAuthFilter(jwtUtil, userRepository, objectMapper, new ApiErrorResponseFactory());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenWithActiveUserSetsAuthenticationAndContinues() throws Exception {
        User user = activeUser("alice");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = requestWithToken(jwtUtil.generateToken(USER_ID, "alice", TENANT_ID, Role.OWNER.name()));
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        AtomicReference<Authentication> authenticationRef = new AtomicReference<>();
        AtomicReference<UUID> tenantRef = new AtomicReference<>();
        FilterChain chain = (req, res) -> {
            chainInvoked.set(true);
            authenticationRef.set(SecurityContextHolder.getContext().getAuthentication());
            tenantRef.set(TenantContext.getTenantId());
        };

        filter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get());
        assertEquals(200, response.getStatus());
        Authentication authentication = authenticationRef.get();
        assertNotNull(authentication);
        assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals(USER_ID, principal.getUserId());
        assertEquals("alice", principal.getUsername());
        assertEquals(TENANT_ID, principal.getTenantId());
        assertEquals(Role.OWNER, principal.getRole());
        assertEquals(TENANT_ID, tenantRef.get());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void expiredTokenReturnsTokenExpired() throws Exception {
        MockHttpServletRequest request = requestWithToken(buildToken(USER_ID, "alice", new Date(System.currentTimeMillis() - 60_000L), SECRET));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "TOKEN_EXPIRED", "Session expired. Please login again.", "/api/v1/transactions");
        verifyNoInteractions(userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void invalidSignatureReturnsInvalidToken() throws Exception {
        MockHttpServletRequest request = requestWithToken(buildToken(USER_ID, "alice", new Date(System.currentTimeMillis() + 60_000L), "abcdefghijklmnopqrstuvwxyz123456"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
        verifyNoInteractions(userRepository);
    }

    @Test
    void malformedTokenReturnsInvalidToken() throws Exception {
        MockHttpServletRequest request = requestWithToken("not-a-jwt");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
        verifyNoInteractions(userRepository);
    }

    @Test
    void tokenWithInvalidUserIdReturnsInvalidToken() throws Exception {
        MockHttpServletRequest request = requestWithToken(buildToken("not-a-uuid", "alice", new Date(System.currentTimeMillis() + 60_000L), SECRET));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
        verifyNoInteractions(userRepository);
    }

    @Test
    void tokenWithMissingUserIdReturnsInvalidToken() throws Exception {
        MockHttpServletRequest request = requestWithToken(buildTokenWithoutUserId("alice", new Date(System.currentTimeMillis() + 60_000L), SECRET));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
        verifyNoInteractions(userRepository);
    }

    @Test
    void tokenForDeletedUserReturnsInvalidToken() throws Exception {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        MockHttpServletRequest request = requestWithToken(jwtUtil.generateToken(USER_ID, "alice", TENANT_ID, Role.USER.name()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
    }

    @Test
    void tokenUsernameMismatchReturnsInvalidToken() throws Exception {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser("current-alice")));

        MockHttpServletRequest request = requestWithToken(jwtUtil.generateToken(USER_ID, "old-alice", TENANT_ID, Role.USER.name()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 401, "INVALID_TOKEN", "Invalid authentication token.", "/api/v1/transactions");
    }

    @Test
    void inactiveUserReturnsAccountInactive() throws Exception {
        User user = activeUser("alice");
        user.setActive(false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        MockHttpServletRequest request = requestWithToken(jwtUtil.generateToken(USER_ID, "alice", TENANT_ID, Role.USER.name()));
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            throw new AssertionError("filter chain should not be invoked");
        });

        assertError(response, 403, "ACCOUNT_INACTIVE", "This account is inactive.", "/api/v1/transactions");
    }

    @Test
    void noBearerTokenContinuesWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        FilterChain chain = (req, res) -> chainInvoked.set(true);

        filter.doFilter(request, response, chain);

        assertTrue(chainInvoked.get());
        assertEquals(200, response.getStatus());
        verifyNoInteractions(userRepository);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertNull(TenantContext.getTenantId());
    }

    private MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/transactions");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return request;
    }

    private User activeUser(String username) {
        User user = new User();
        user.setId(USER_ID);
        user.setTenantId(TENANT_ID);
        user.setUsername(username);
        user.setPassword("encoded-password");
        user.setRole(Role.OWNER);
        user.setActive(true);
        return user;
    }

    private String buildToken(UUID userId, String subject, Date expiration, String secret) {
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userId.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 60_000L))
                .expiration(expiration)
                .signWith(signingKey(secret))
                .compact();
    }

    private String buildToken(String userIdClaim, String subject, Date expiration, String secret) {
        return Jwts.builder()
                .subject(subject)
                .claim("userId", userIdClaim)
                .issuedAt(new Date(System.currentTimeMillis() - 60_000L))
                .expiration(expiration)
                .signWith(signingKey(secret))
                .compact();
    }

    private String buildTokenWithoutUserId(String subject, Date expiration, String secret) {
        return Jwts.builder()
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis() - 60_000L))
                .expiration(expiration)
                .signWith(signingKey(secret))
                .compact();
    }

    private SecretKey signingKey(String secret) {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    private void assertError(MockHttpServletResponse response,
                             int expectedStatus,
                             String expectedCode,
                             String expectedMessage,
                             String expectedPath) throws Exception {
        JsonNode json = objectMapper.readTree(response.getContentAsString());
        assertEquals(expectedStatus, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals(expectedStatus, json.get("status").asInt());
        assertEquals(expectedCode, json.get("code").asText());
        assertEquals(expectedMessage, json.get("message").asText());
        assertEquals(expectedPath, json.get("path").asText());
        assertFalse(json.get("traceId").asText().isBlank());
        assertTrue(json.get("details").isObject());
    }
}
