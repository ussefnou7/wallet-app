package com.wallet.walletapp.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile upload(MultipartFile file, String folder);

    void delete(String storageKey);
}
