package com.wallet.walletapp.transaction;

import com.wallet.walletapp.transaction.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTenantId(transaction.getTenantId());
        response.setWalletId(transaction.getWalletId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setPercent(transaction.getPercent());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }
}
