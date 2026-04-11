package com.wallet.walletapp.wallet;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.dto.CreateWalletRequest;
import com.wallet.walletapp.wallet.dto.UpdateWalletRequest;
import com.wallet.walletapp.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;
    private final WalletMapper walletMapper;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        UserPrincipal user = currentUser();

        Wallet wallet = new Wallet();
        wallet.setTenantId(request.getTenantId());
        wallet.setBranchId(request.getBranchId());
        wallet.setName(request.getName());
        wallet.setType(request.getType());
        wallet.setNumber(request.getNumber());
        wallet.setBalance(request.getBalance());
        wallet.setDailyLimit(request.getDailyLimit());
        wallet.setMonthlyLimit(request.getMonthlyLimit());

        wallet = walletRepository.save(wallet);
        log.info("Wallet '{}' created for tenant {}", wallet.getName(), wallet.getTenantId());
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets() {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        List<Wallet> wallets;
        if (user.getRole() == Role.SYSTEM_ADMIN) {
            wallets = walletRepository.findAll();
        } else {
            wallets = walletRepository.findByTenantId(tenantId);
        }

        return wallets.stream().map(walletMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(UUID id) {
        Wallet wallet = findWalletWithAccess(id);
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public WalletResponse updateWallet(UUID id, UpdateWalletRequest request) {
        Wallet wallet = findWalletWithAccess(id);
        wallet.setName(request.getName());
        wallet.setActive(request.isActive());
        wallet = walletRepository.save(wallet);
        log.info("Wallet {} updated", id);
        return walletMapper.toResponse(wallet);
    }

    @Override
    @Transactional
    public void deleteWallet(UUID id) {
        Wallet wallet = findWalletWithAccess(id);
        walletRepository.delete(wallet);
        log.info("Wallet {} deleted", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletType> getWalletTypes() {
        return List.of(WalletType.values());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getWalletsByTenantIdAndType(UUID tenantId, WalletType type) {
        return walletRepository.findByTenantIdAndType(tenantId, type)
                .stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());
    }

    private Wallet findWalletWithAccess(UUID walletId) {
        UserPrincipal user = currentUser();
        Wallet wallet = walletRepository.findByIdAndTenantId(walletId, user.getTenantId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        if (user.getRole() == Role.USER) {
            boolean hasAccess = walletUserRepository.existsByUserIdAndWalletIdAndTenantId(
                    user.getUserId(), walletId, user.getTenantId());
            if (!hasAccess) {
                throw new UnauthorizedException("Access denied to wallet");
            }
        }
        return wallet;
    }

    private List<WalletResponse> getWalletsByTenantId(UUID tenantId) {
        return walletRepository.findByTenantId(tenantId)
                .stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getWalletsByBranchId(UUID branchId) {
        return walletRepository.findByBranchId(branchId)
                .stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<WalletResponse> getWalletsByBranchIdAndType(UUID branchId, WalletType type) {
        return walletRepository.findByBranchIdAndType(branchId,type)
                .stream()
                .map(walletMapper::toResponse)
                .collect(Collectors.toList());
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
