package com.wallet.walletapp.attachment;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "attachments", indexes = {
        @Index(name = "idx_attachments_module_reference", columnList = "module_type, reference_id"),
        @Index(name = "idx_attachments_tenant_id", columnList = "tenant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment extends TenantAwareEntity {

    @Column(nullable = false)
    private UUID uploadedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttachmentModuleType moduleType;

    @Column(nullable = false)
    private UUID referenceId;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StorageProvider storageProvider;

    @Column(nullable = false, unique = true)
    private String storageKey;

    @Column(nullable = false, length = 1000)
    private String url;
}
