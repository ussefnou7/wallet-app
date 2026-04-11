package com.wallet.walletapp.transaction;

import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    List<TransactionResponse> getAllTransactions(UUID walletId, TransactionType type,
                                                  LocalDateTime dateFrom, LocalDateTime dateTo);

    TransactionResponse getTransactionById(UUID id);
}
