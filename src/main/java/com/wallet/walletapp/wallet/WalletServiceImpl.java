package com.wallet.walletapp.wallet;

import com.wallet.walletapp.auth.UserPrincipal;
import com.wallet.walletapp.exception.EntityNotFoundException;
import com.wallet.walletapp.exception.UnauthorizedException;
import com.wallet.walletapp.plan.SubscriptionAccessService;
import com.wallet.walletapp.user.Role;
import com.wallet.walletapp.wallet.dto.CreateWalletRequest;
import com.wallet.walletapp.wallet.dto.UpdateWalletRequest;
import com.wallet.walletapp.wallet.dto.WalletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final WalletUserRepository walletUserRepository;
    private final WalletConsumptionService walletConsumptionService;
    private final WalletConsumptionRepository walletConsumptionRepository;
    private final WalletMapper walletMapper;
    private final SubscriptionAccessService subscriptionAccessService;

    @Override
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getRole() == Role.SYSTEM_ADMIN ? request.getTenantId() : user.getTenantId();
        subscriptionAccessService.validateValidSubscription(tenantId);
        subscriptionAccessService.validateCreateWalletLimit(tenantId);

        Wallet wallet = new Wallet();
        wallet.setTenantId(tenantId);
        wallet.setBranchId(request.getBranchId());
        wallet.setName(request.getName());
        wallet.setType(request.getType());
        wallet.setNumber(request.getNumber());
        wallet.setBalance(request.getBalance());
        wallet.setDailyLimit(request.getDailyLimit());
        wallet.setMonthlyLimit(request.getMonthlyLimit());

        wallet = walletRepository.save(wallet);
        wallet.setConsumption(walletConsumptionService.createForWallet(wallet));
        log.info("Wallet '{}' created for tenant {}", wallet.getName(), wallet.getTenantId());
        return walletMapper.toResponse(
                walletRepository.findReadById(wallet.getId())
                        .orElseThrow(() -> new IllegalStateException("Wallet not found after create")),
                wallet.getConsumption()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<WalletResponse> getAllWallets(Integer page, Integer size) {
        UserPrincipal user = currentUser();
        UUID tenantId = user.getTenantId();

        List<WalletReadProjection> wallets;
        Pageable pageable = buildPageable(page, size);
        if (pageable != null) {
            if (user.getRole() == Role.SYSTEM_ADMIN) {
                wallets = walletRepository.findAllForRead(pageable).getContent();
            } else {
                wallets = walletRepository.findAllByTenantIdForRead(tenantId, pageable).getContent();
            }
        } else if (user.getRole() == Role.SYSTEM_ADMIN) {
            wallets = walletRepository.findAllForRead();
        } else {
            wallets = walletRepository.findAllByTenantIdForRead(tenantId);
        }

        return toWalletResponses(wallets);
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponse getWalletById(UUID id) {
        Wallet wallet = withCurrentConsumption(findWalletWithAccess(id));
        WalletReadProjection projection = walletRepository.findReadByIdAndTenantId(id, wallet.getTenantId()).orElseThrow(() -> new EntityNotFoundException("Wallet not found"));
        return walletMapper.toResponse(projection, wallet.getConsumption());
    }

    @Override
    @Transactional
    public WalletResponse updateWallet(UUID id, UpdateWalletRequest request) {
        Wallet wallet = findWalletWithAccess(id);
        wallet.setName(request.getName());
        wallet.setActive(request.isActive());
        wallet = walletRepository.save(wallet);
        log.info("Wallet {} updated", id);
        Wallet refreshedWallet = withCurrentConsumption(wallet);
        return walletMapper.toResponse(walletRepository.findReadById(refreshedWallet.getId()).orElseThrow(() -> new IllegalStateException("Wallet not found after update")), refreshedWallet.getConsumption());
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
        return toWalletResponses(walletRepository.findByTenantIdAndTypeForRead(tenantId, type));
    }

    private Wallet findWalletWithAccess(UUID walletId) {
        UserPrincipal user = currentUser();
        Wallet wallet = walletRepository.findByIdAndTenantId(walletId, user.getTenantId()).orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        if (user.getRole() == Role.USER) {
            boolean hasAccess = walletUserRepository.existsByUserIdAndWalletIdAndTenantId(user.getUserId(), walletId, user.getTenantId());
            if (!hasAccess) {
                throw new UnauthorizedException("Access denied to wallet");
            }
        }
        return wallet;
    }

    private List<WalletResponse> getWalletsByTenantId(UUID tenantId) {
        return toWalletResponses(walletRepository.findAllByTenantIdForRead(tenantId));
    }

    public List<WalletResponse> getWalletsByBranchId(UUID branchId) {
        return toWalletResponses(walletRepository.findByBranchIdForRead(branchId));
    }

    public List<WalletResponse> getWalletsByBranchIdAndType(UUID branchId, WalletType type) {
        return toWalletResponses(walletRepository.findByBranchIdAndTypeForRead(branchId, type));
    }

    private UserPrincipal currentUser() {
        return (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Wallet withCurrentConsumption(Wallet wallet) {
        wallet.setConsumption(walletConsumptionService.getByWallet(wallet));
        return wallet;
    }

    private List<WalletResponse> toWalletResponses(List<WalletReadProjection> projections) {
        Map<UUID, WalletConsumption> consumptionsByWalletId = walletConsumptionRepository.findAllByWalletIdIn(projections.stream().map(WalletReadProjection::getId).toList()).stream().collect(Collectors.toMap(WalletConsumption::getWalletId, Function.identity()));

        return projections.stream().map(projection -> walletMapper.toResponse(projection, consumptionsByWalletId.get(projection.getId()))).collect(Collectors.toList());
    }

    private Pageable buildPageable(Integer page, Integer size) {
        if (page == null && size == null) {
            return null;
        }
        int resolvedPage = page != null ? page : 0;
        int resolvedSize = size != null ? size : 20;
        if (resolvedPage < 0 || resolvedSize < 1) {
            throw new IllegalArgumentException("Invalid pagination parameters");
        }
        return PageRequest.of(resolvedPage, resolvedSize);
    }
}
