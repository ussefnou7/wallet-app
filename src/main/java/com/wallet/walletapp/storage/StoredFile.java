package com.wallet.walletapp.storage;

public record StoredFile(
        String storageKey,
        String url,
        String contentType,
        Long sizeBytes,
        String originalFileName
) {
}
