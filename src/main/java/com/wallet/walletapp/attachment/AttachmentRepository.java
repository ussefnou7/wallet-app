package com.wallet.walletapp.attachment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    Optional<Attachment> findFirstByModuleTypeAndReferenceIdOrderByCreatedAtDesc(AttachmentModuleType moduleType, UUID referenceId);

    List<Attachment> findAllByModuleTypeAndReferenceIdIn(AttachmentModuleType moduleType, Collection<UUID> referenceIds);
}
