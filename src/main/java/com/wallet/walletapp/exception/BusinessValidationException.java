package com.wallet.walletapp.exception;

import java.util.Map;

public class BusinessValidationException extends BusinessException {

    public BusinessValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessValidationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessValidationException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(errorCode, message, details);
    }

    public BusinessValidationException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
