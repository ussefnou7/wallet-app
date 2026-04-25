package com.wallet.walletapp.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR", "Request validation failed", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "The request is invalid", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "Authentication is required to access this resource", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "You are not allowed to perform this action", HttpStatus.FORBIDDEN),
    ENTITY_NOT_FOUND("ENTITY_NOT_FOUND", "The requested resource was not found", HttpStatus.NOT_FOUND),
    TENANT_NOT_FOUND("TENANT_NOT_FOUND", "Tenant not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_NOT_FOUND", "User not found", HttpStatus.NOT_FOUND),
    WALLET_NOT_FOUND("WALLET_NOT_FOUND", "Wallet not found", HttpStatus.NOT_FOUND),
    BRANCH_NOT_FOUND("BRANCH_NOT_FOUND", "Branch not found", HttpStatus.NOT_FOUND),
    TRANSACTION_NOT_FOUND("TRANSACTION_NOT_FOUND", "Transaction not found", HttpStatus.NOT_FOUND),
    DUPLICATED_TRANSACTION("DUPLICATED_TRANSACTION", "This transaction was already submitted", HttpStatus.CONFLICT),
    WALLET_LIMIT_EXCEEDED("WALLET_LIMIT_EXCEEDED", "Wallet limit exceeded for the current plan", HttpStatus.CONFLICT),
    INSUFFICIENT_BALANCE("INSUFFICIENT_BALANCE", "Insufficient balance", HttpStatus.CONFLICT),
    DATA_CONFLICT("DATA_CONFLICT", "The request conflicts with existing data", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;
}
