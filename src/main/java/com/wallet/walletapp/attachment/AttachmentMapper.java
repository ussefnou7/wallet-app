package com.wallet.walletapp.attachment;

import com.wallet.walletapp.attachment.dto.AttachmentResponse;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentResponse toResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                attachment.getUrl()
        );
    }
}
