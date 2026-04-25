package com.wallet.walletapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final String DUPLICATED_TRANSACTION_CONSTRAINT = "uk_transactions_tenant_external_transaction";

    private final ApiErrorResponseFactory errorResponseFactory;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ApiErrorResponse response = errorResponseFactory.create(
                request,
                ex.getErrorCode(),
                ex.getMessage(),
                ex.getDetails()
        );
        log.warn("Business error {} on {} [{}]: {}", response.getCode(), request.getRequestURI(), response.getTraceId(), ex.getMessage());
        return ResponseEntity.status(ex.getErrorCode().getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> details.put(error.getField(), resolveValidationMessage(error)));
        ex.getBindingResult().getGlobalErrors().forEach(error -> details.put(error.getObjectName(), error.getDefaultMessage()));
        return build(request, ErrorCode.VALIDATION_ERROR, null, details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, Object> details = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            details.put(lastPathSegment(violation), violation.getMessage());
        }
        return build(request, ErrorCode.VALIDATION_ERROR, null, details);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return build(request, ErrorCode.UNAUTHORIZED, "Invalid username or password", Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(request, ErrorCode.FORBIDDEN, null, Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(request, ErrorCode.UNAUTHORIZED, null, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        ErrorCode errorCode = resolveDataIntegrityErrorCode(ex);
        return build(request, errorCode, errorCode.getDefaultMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(request, ErrorCode.BAD_REQUEST, "Request body is malformed or unreadable", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return build(
                request,
                ErrorCode.BAD_REQUEST,
                "Missing required request parameter",
                Map.of(ex.getParameterName(), "parameter is required")
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(request, ErrorCode.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        return build(request, ErrorCode.BAD_REQUEST, ex.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        ApiErrorResponse response = errorResponseFactory.create(
                request,
                ErrorCode.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage(),
                Map.of()
        );
        log.error("Unexpected error on {} [{}]", request.getRequestURI(), response.getTraceId(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpServletRequest request,
                                                   ErrorCode errorCode,
                                                   String message,
                                                   Map<String, Object> details) {
        ApiErrorResponse response = errorResponseFactory.create(request, errorCode, message, details);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }

    private String resolveValidationMessage(FieldError error) {
        return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value";
    }

    private String lastPathSegment(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        int separatorIndex = path.lastIndexOf('.');
        return separatorIndex >= 0 ? path.substring(separatorIndex + 1) : path;
    }

    private ErrorCode resolveDataIntegrityErrorCode(DataIntegrityViolationException ex) {
        String message = NestedExceptionUtils.getMostSpecificCause(ex).getMessage();
        if (message == null) {
            return ErrorCode.DATA_CONFLICT;
        }

        String normalizedMessage = message.toLowerCase();
        if (normalizedMessage.contains(DUPLICATED_TRANSACTION_CONSTRAINT)
                || normalizedMessage.contains("external_transaction_id")) {
            return ErrorCode.DUPLICATED_TRANSACTION;
        }

        if (normalizedMessage.contains("duplicate key")
                || normalizedMessage.contains("unique constraint")
                || normalizedMessage.contains("unique index")
                || normalizedMessage.contains("unique")) {
            return ErrorCode.DATA_CONFLICT;
        }

        return ErrorCode.DATA_CONFLICT;
    }
}
