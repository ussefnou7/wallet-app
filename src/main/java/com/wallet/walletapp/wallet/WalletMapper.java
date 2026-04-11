package com.wallet.walletapp.wallet;

import com.wallet.walletapp.wallet.dto.WalletResponse;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletResponse toResponse(Wallet wallet) {
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setTenantId(wallet.getTenantId());
        response.setBranchId(wallet.getBranchId());
        response.setName(wallet.getName());
        response.setNumber(wallet.getNumber());
        response.setBalance(wallet.getBalance());
        response.setCashProfit(wallet.getCashProfit());
        response.setWalletProfit(wallet.getWalletProfit());
        response.setType(wallet.getType());
        response.setActive(wallet.isActive());
        response.setCreatedAt(wallet.getCreatedAt());
        response.setUpdatedAt(wallet.getUpdatedAt());
        return response;
    }
}
