package com.wallet.walletapp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class ApiErrorResponseFactory {

    public ApiErrorResponse create(HttpServletRequest request,
                                   ErrorCode errorCode,
                                   String message,
                                   Map<String, Object> details) {
        return create(request, errorCode, message, details, resolveTraceId());
    }

    public ApiErrorResponse create(HttpServletRequest request,
                                   ErrorCode errorCode,
                                   String message,
                                   Map<String, Object> details,
                                   String traceId) {
        Map<String, Object> safeDetails = details == null || details.isEmpty()
                ? Map.of()
                : new LinkedHashMap<>(details);

        return ApiErrorResponse.builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toString())
                .status(errorCode.getHttpStatus().value())
                .code(errorCode.getCode())
                .message(hasText(message) ? message : errorCode.getDefaultMessage())
                .path(request != null ? request.getRequestURI() : "")
                .details(safeDetails)
                .traceId(traceId)
                .build();
    }

    public String resolveTraceId() {
        String traceId = firstNonBlank(
                MDC.get("traceId"),
                MDC.get("trace_id"),
                MDC.get("X-B3-TraceId"),
                MDC.get("x-b3-traceid")
        );

        if (traceId == null) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put("traceId", traceId);
        return traceId;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
