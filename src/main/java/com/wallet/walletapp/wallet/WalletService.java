package com.wallet.walletapp.wallet;

import com.wallet.walletapp.wallet.dto.CreateWalletRequest;
import com.wallet.walletapp.wallet.dto.UpdateWalletRequest;
import com.wallet.walletapp.wallet.dto.WalletResponse;

import java.util.List;
import java.util.UUID;

public interface WalletService {

    WalletResponse createWallet(CreateWalletRequest request);

    List<WalletResponse> getAllWallets();

    WalletResponse getWalletById(UUID id);

    WalletResponse updateWallet(UUID id, UpdateWalletRequest request);

    void deleteWallet(UUID id);

    List<WalletType> getWalletTypes();

    List<WalletResponse> getWalletsByTenantIdAndType(UUID tenantId, WalletType type);

    List<WalletResponse> getWalletsByBranchId(UUID branchId);

    List<WalletResponse> getWalletsByBranchIdAndType(UUID branchId, WalletType type);
}
