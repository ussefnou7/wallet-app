package com.wallet.walletapp.renewal;

import com.wallet.walletapp.renewal.dto.CreateRenewalRequest;
import com.wallet.walletapp.renewal.dto.RenewalRequestResponse;
import com.wallet.walletapp.renewal.dto.ReviewRenewalRequest;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface RenewalRequestService {

    RenewalRequestResponse createRequest(CreateRenewalRequest request);

    List<RenewalRequestResponse> getMyRequests();

    RenewalRequestResponse getMyRequest(UUID requestId);

    List<RenewalRequestResponse> getAdminRequests(@Nullable RenewalRequestStatus status,
                                                  @Nullable UUID tenantId);

    RenewalRequestResponse approveRequest(UUID requestId, @Nullable ReviewRenewalRequest request);

    RenewalRequestResponse rejectRequest(UUID requestId, @Nullable ReviewRenewalRequest request);
}
