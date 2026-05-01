package com.wallet.walletapp.transaction;

import com.wallet.walletapp.common.dto.PageResponse;
import com.wallet.walletapp.transaction.dto.CreateTransactionRequest;
import com.wallet.walletapp.transaction.dto.TransactionReadResponse;
import com.wallet.walletapp.transaction.dto.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionReadResponse>> getAllTransactions(
            @RequestParam(required = false) UUID walletId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getAllTransactions(walletId, type, dateFrom, dateTo, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionReadResponse> getTransactionById(@PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }
}
