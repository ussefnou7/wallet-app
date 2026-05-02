package com.wallet.walletapp.wallet;

import com.wallet.walletapp.wallet.dto.CreateWalletRequest;
import com.wallet.walletapp.wallet.dto.UpdateWalletRequest;
import com.wallet.walletapp.wallet.dto.WalletResponse;
import com.wallet.walletapp.wallet.profit.CollectProfitRequest;
import com.wallet.walletapp.wallet.profit.ProfitCollectionResponse;
import com.wallet.walletapp.wallet.profit.ProfitCollectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private  final ProfitCollectionService profitCollectionService;

    @GetMapping("/types")
    public ResponseEntity<List<WalletType>> getWalletTypes() {
      return ResponseEntity.ok(walletService.getWalletTypes());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));
    }

    @GetMapping
    public ResponseEntity<List<WalletResponse>> getAllWallets(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return ResponseEntity.ok(walletService.getAllWallets(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> getWalletById(@PathVariable UUID id) {
        return ResponseEntity.ok(walletService.getWalletById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<WalletResponse> updateWallet(@PathVariable UUID id,
                                                        @Valid @RequestBody UpdateWalletRequest request) {
        return ResponseEntity.ok(walletService.updateWallet(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<Void> deleteWallet(@PathVariable UUID id) {
        walletService.deleteWallet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{walletId}/collect-profit")
    @PreAuthorize("hasAnyRole('OWNER', 'SYSTEM_ADMIN')")
    public ResponseEntity<ProfitCollectionResponse> collectProfit(
            @PathVariable UUID walletId,
            @RequestBody @Valid CollectProfitRequest request
    ) {
        return ResponseEntity.ok(profitCollectionService.collectProfit(walletId, request));
    }

    
}
