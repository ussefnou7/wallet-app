package com.wallet.walletapp.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    @Builder.Default
    private Map<String, Object> details = Map.of();
    private String traceId;
}
