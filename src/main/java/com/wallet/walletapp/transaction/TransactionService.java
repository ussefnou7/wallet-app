package com.wallet.walletapp.transaction;

import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getAllTransactions(@Nullable UUID walletId, @Nullable TransactionType type,
                                                 @Nullable LocalDateTime dateFrom, @Nullable LocalDateTime dateTo);

    TransactionResponse getTransactionById(UUID id);
}
