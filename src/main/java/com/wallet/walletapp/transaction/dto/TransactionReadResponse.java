package com.wallet.walletapp.transaction.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionReadResponse extends TransactionResponse {
    private String walletName;
    private String createdByUsername;
}
