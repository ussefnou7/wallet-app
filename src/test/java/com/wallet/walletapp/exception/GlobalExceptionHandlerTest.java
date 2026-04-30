package com.wallet.walletapp.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler(new ApiErrorResponseFactory()))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void validationErrorResponseShape() throws Exception {
        ValidationRequest request = new ValidationRequest();
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.details.amount").value("must be greater than 0"))
                .andExpect(jsonPath("$.details.walletId").value("must not be null"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void notFoundResponseUsesBusinessCode() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Wallet not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void duplicatedTransactionConflictResponse() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("DUPLICATED_TRANSACTION"))
                .andExpect(jsonPath("$.message").value("This transaction was already submitted"))
                .andExpect(jsonPath("$.path").value("/test/duplicate"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void badCredentialsReturnsUnauthorizedContract() throws Exception {
        mockMvc.perform(get("/test/bad-credentials"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Invalid username or password"))
                .andExpect(jsonPath("$.path").value("/test/bad-credentials"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void accessDeniedReturnsForbiddenContract() throws Exception {
        mockMvc.perform(get("/test/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("You are not allowed to perform this action"))
                .andExpect(jsonPath("$.path").value("/test/forbidden"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @Test
    void generic500ReturnsSafeResponse() throws Exception {
        mockMvc.perform(get("/test/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please try again later."))
                .andExpect(jsonPath("$.message").value(not("sensitive-db-stacktrace")))
                .andExpect(jsonPath("$.path").value("/test/boom"))
                .andExpect(jsonPath("$.traceId", not(blankOrNullString())));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping("/validation")
        void validation(@Valid @RequestBody ValidationRequest request) {
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new EntityNotFoundException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found");
        }

        @GetMapping("/duplicate")
        void duplicate() {
            throw new DataIntegrityViolationException(
                    "duplicate transaction",
                    new RuntimeException("duplicate key value violates unique constraint \"uk_transactions_tenant_external_transaction\"")
            );
        }

        @GetMapping("/bad-credentials")
        void badCredentials() {
            throw new BadCredentialsException("Bad credentials");
        }

        @GetMapping("/forbidden")
        void forbidden() {
            throw new AccessDeniedException("Forbidden");
        }

        @GetMapping("/boom")
        void boom() {
            throw new RuntimeException("sensitive-db-stacktrace");
        }
    }

    static class ValidationRequest {

        @NotNull(message = "must not be null")
        private UUID walletId;

        @NotNull(message = "must not be null")
        @DecimalMin(value = "0", inclusive = false, message = "must be greater than 0")
        private BigDecimal amount;

        public UUID getWalletId() {
            return walletId;
        }

        public void setWalletId(UUID walletId) {
            this.walletId = walletId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
