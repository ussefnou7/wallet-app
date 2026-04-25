package com.wallet.walletapp.exception;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

@Getter
public abstract class AppException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    protected AppException(ErrorCode errorCode) {
        this(errorCode, null, null);
    }

    protected AppException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    protected AppException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(hasText(message) ? message : Objects.requireNonNull(errorCode, "errorCode must not be null").getDefaultMessage());
        this.errorCode = errorCode;
        this.details = details == null || details.isEmpty() ? Map.of() : Map.copyOf(details);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
