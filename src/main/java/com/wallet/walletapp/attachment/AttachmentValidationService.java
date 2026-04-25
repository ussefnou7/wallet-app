package com.wallet.walletapp.attachment;

import com.wallet.walletapp.config.AttachmentProperties;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.BusinessValidationException;
import com.wallet.walletapp.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AttachmentValidationService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final AttachmentProperties attachmentProperties;

    public void validateImage(MultipartFile file) {
        if (file == null) {
            return;
        }

        if (file.isEmpty()) {
            throw new BusinessValidationException(
                    ErrorCode.BAD_REQUEST,
                    "Uploaded file is empty",
                    Map.of("image", "must not be empty")
            );
        }

        if (file.getSize() > attachmentProperties.getMaxFileSize().toBytes()) {
            throw new BusinessException(
                    ErrorCode.FILE_TOO_LARGE,
                    null,
                    Map.of("maxSizeBytes", attachmentProperties.getMaxFileSize().toBytes())
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(
                    ErrorCode.UNSUPPORTED_FILE_TYPE,
                    "Only JPEG, PNG, and WEBP images are allowed",
                    Map.of("image", contentType != null ? contentType : "unknown")
            );
        }
    }
}
