package com.wallet.walletapp.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.walletapp.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMapper {

    private final ObjectMapper objectMapper;

    public NotificationResponse toResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setType(notification.getType());
        response.setPriority(notification.getPriority());
        response.setSeverity(notification.getSeverity());
        response.setTitleKey(notification.getTitleKey());
        response.setMessageKey(notification.getMessageKey());
        response.setPayload(parsePayload(notification.getPayloadJson()));
        response.setTargetType(notification.getTargetType());
        response.setTargetId(notification.getTargetId());
        response.setCreatedAt(notification.getCreatedAt());
        return response;
    }

    private Object parsePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(payloadJson, Object.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Invalid notification payload json", ex);
        }
    }
}
