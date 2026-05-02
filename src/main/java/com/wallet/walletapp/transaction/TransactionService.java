package com.wallet.walletapp.transaction;

import com.wallet.walletapp.common.dto.PageResponse;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionService {

    TransactionResponse createTransaction(CreateTransactionRequest request);

    PageResponse<TransactionReadResponse> getAllTransactions(@Nullable UUID walletId,
                                                             @Nullable TransactionType type,
                                                             @Nullable LocalDateTime dateFrom,
                                                             @Nullable LocalDateTime dateTo,
                                                             @Nullable UUID createdBy,
                                                             int page,
                                                             int size);

    TransactionReadResponse getTransactionById(UUID id);
}
