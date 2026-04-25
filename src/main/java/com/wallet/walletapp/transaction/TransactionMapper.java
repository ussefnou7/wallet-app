package com.wallet.walletapp.transaction;

import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTenantId(transaction.getTenantId());
        response.setWalletId(transaction.getWalletId());
        response.setExternalTransactionId(transaction.getExternalTransactionId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setPercent(transaction.getPercent());
        response.setPhoneNumber(transaction.getPhoneNumber());
        response.setCash(transaction.isCash());
        response.setDescription(transaction.getDescription());
        response.setOccurredAt(transaction.getOccurredAt());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setUpdatedAt(transaction.getUpdatedAt());
        return response;
    }

    public TransactionReadResponse toReadResponse(TransactionReadProjection projection) {
        TransactionReadResponse response = new TransactionReadResponse();
        response.setId(projection.getId());
        response.setTenantId(projection.getTenantId());
        response.setWalletId(projection.getWalletId());
        response.setWalletName(projection.getWalletName());
        response.setExternalTransactionId(projection.getExternalTransactionId());
        response.setAmount(projection.getAmount());
        response.setType(projection.getType());
        response.setPercent(projection.getPercent());
        response.setPhoneNumber(projection.getPhoneNumber());
        response.setCash(Boolean.TRUE.equals(projection.getCash()));
        response.setDescription(projection.getDescription());
        response.setOccurredAt(projection.getOccurredAt());
        response.setCreatedAt(projection.getCreatedAt());
        response.setUpdatedAt(projection.getUpdatedAt());
        response.setCreatedByUsername(projection.getCreatedByUsername());
        return response;
    }

    public Transaction toEntity(CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setWalletId(request.getWalletId());
        transaction.setExternalTransactionId(request.getExternalTransactionId());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCash(request.isCash());
        transaction.setPhoneNumber(request.getPhoneNumber());
        transaction.setPercent(request.getPercent());
        transaction.setDescription(request.getDescription());
        transaction.setOccurredAt(request.getOccurredAt());
        return transaction;
    }
}
