package com.wallet.walletapp.attachment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private UUID attachmentId;
    private String originalFileName;
    private String contentType;
    private Long sizeBytes;
    private String url;
}
