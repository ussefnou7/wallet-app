package com.wallet.walletapp.attachment;

import com.wallet.walletapp.attachment.dto.AttachmentResponse;
import com.wallet.walletapp.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentMapper attachmentMapper;

    @Override
    @Transactional
    public Attachment createAttachment(UUID tenantId,
                                       UUID uploadedBy,
                                       AttachmentModuleType moduleType,
                                       UUID referenceId,
                                       StoredFile storedFile) {
        Attachment attachment = Attachment.builder()
                .uploadedBy(uploadedBy)
                .moduleType(moduleType)
                .referenceId(referenceId)
                .originalFileName(storedFile.originalFileName())
                .contentType(storedFile.contentType())
                .sizeBytes(storedFile.sizeBytes())
                .storageProvider(StorageProvider.CLOUDINARY)
                .storageKey(storedFile.storageKey())
                .url(storedFile.url())
                .build();
        attachment.setTenantId(tenantId);
        return attachmentRepository.save(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AttachmentResponse> getAttachmentResponse(AttachmentModuleType moduleType, UUID referenceId) {
        return attachmentRepository.findFirstByModuleTypeAndReferenceIdOrderByCreatedAtDesc(moduleType, referenceId)
                .map(attachmentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, AttachmentResponse> getAttachmentResponses(AttachmentModuleType moduleType, Collection<UUID> referenceIds) {
        if (referenceIds == null || referenceIds.isEmpty()) {
            return Map.of();
        }

        return attachmentRepository.findAllByModuleTypeAndReferenceIdIn(moduleType, referenceIds).stream()
                .collect(Collectors.toMap(
                        Attachment::getReferenceId,
                        attachmentMapper::toResponse,
                        (existing, replacement) -> existing
                ));
    }
}
