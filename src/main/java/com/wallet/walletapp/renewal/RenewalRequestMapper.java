package com.wallet.walletapp.renewal;

import com.wallet.walletapp.renewal.dto.RenewalRequestResponse;
import org.springframework.stereotype.Component;

@Component
public class RenewalRequestMapper {

    public RenewalRequestResponse toResponse(RenewalRequestReadProjection projection) {
        return new RenewalRequestResponse(
                projection.getId(),
                projection.getTenantId(),
                projection.getRequestedBy(),
                projection.getPhoneNumber(),
                projection.getAmount(),
                projection.getPeriodMonths(),
                projection.getStatus(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getReviewedAt(),
                projection.getReviewedBy(),
                projection.getAdminNote(),
                projection.getTenantName(),
                projection.getRequestedByName(),
                projection.getReviewedByName()
        );
    }

    public RenewalRequestResponse toResponse(RenewalRequest request) {
        return new RenewalRequestResponse(
                request.getId(),
                request.getTenantId(),
                request.getRequestedBy(),
                request.getPhoneNumber(),
                request.getAmount(),
                request.getPeriodMonths(),
                request.getStatus(),
                request.getCreatedAt(),
                request.getUpdatedAt(),
                request.getReviewedAt(),
                request.getReviewedBy(),
                request.getAdminNote(),
                null,
                null,
                null
        );
    }
}
