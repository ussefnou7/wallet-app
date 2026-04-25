package com.wallet.walletapp.attachment;

import com.wallet.walletapp.attachment.dto.AttachmentResponse;
import com.wallet.walletapp.storage.StoredFile;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentService {

    Attachment createAttachment(UUID tenantId,
                                UUID uploadedBy,
                                AttachmentModuleType moduleType,
                                UUID referenceId,
                                StoredFile storedFile);

    Optional<AttachmentResponse> getAttachmentResponse(AttachmentModuleType moduleType, UUID referenceId);

    Map<UUID, AttachmentResponse> getAttachmentResponses(AttachmentModuleType moduleType, Collection<UUID> referenceIds);
}
