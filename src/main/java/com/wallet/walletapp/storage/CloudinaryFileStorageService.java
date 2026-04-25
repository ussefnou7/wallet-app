package com.wallet.walletapp.storage;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.wallet.walletapp.exception.BusinessException;
import com.wallet.walletapp.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
@ConditionalOnBean(Cloudinary.class)
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements FileStorageService {

    private final Cloudinary cloudinary;

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image"
                    )
            );

            return new StoredFile(
                    String.valueOf(response.get("public_id")),
                    String.valueOf(response.get("secure_url")),
                    file.getContentType(),
                    file.getSize(),
                    file.getOriginalFilename()
            );
        } catch (IOException | RuntimeException ex) {
            log.error("Cloudinary upload failed for file {}", file.getOriginalFilename(), ex);
            throw new BusinessException(
                    ErrorCode.FILE_UPLOAD_FAILED,
                    "Failed to upload file to Cloudinary",
                    Map.of("image", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown")
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            cloudinary.uploader().destroy(
                    storageKey,
                    ObjectUtils.asMap(
                            "invalidate", true,
                            "resource_type", "image"
                    )
            );
        } catch (IOException | RuntimeException ex) {
            log.warn("Failed to delete Cloudinary asset {}", storageKey, ex);
        }
    }
}
