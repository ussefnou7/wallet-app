package com.wallet.walletapp.wallet.consumption;

import com.wallet.walletapp.reporting.wallet.WalletConsumptionReportReadModel;
import jakarta.persistence.LockModeType;
import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletConsumptionRepository extends JpaRepository<WalletConsumption, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletConsumption> findByWalletId(UUID walletId);

    List<WalletConsumption> findAllByWalletIdIn(Collection<UUID> walletIds);

    @Query("""
            select new com.wallet.walletapp.reporting.wallet.WalletConsumptionReportReadModel(
                wc.walletId,
                w.tenantId,
                tenant.name,
                w.branchId,
                branch.name,
                w.id,
                w.name,
                wc.dailyConsumed,
                wc.monthlyConsumed,
                w.dailyLimit,
                w.monthlyLimit,
                wc.lastProcessedOccurredAt,
                w.active
            )
            from WalletConsumption wc
            join wc.wallet w
            join Tenant tenant
                on tenant.id = w.tenantId
            left join Branch branch
                on branch.id = w.branchId
               and branch.tenantId = w.tenantId
            where w.tenantId = :tenantId
              and w.id = coalesce(:walletId, w.id)
              and w.branchId = coalesce(:branchId, w.branchId)
              and w.active = coalesce(:active, w.active)
            order by w.name asc, w.id asc
            """)
    List<WalletConsumptionReportReadModel> findReportByTenantId(@Param("tenantId") UUID tenantId,
                                                                @Param("walletId") @Nullable UUID walletId,
                                                                @Param("branchId") @Nullable UUID branchId,
                                                                @Param("active") @Nullable Boolean active);

    @Query("""
            select new com.wallet.walletapp.reporting.wallet.WalletConsumptionReportReadModel(
                wc.walletId,
                w.tenantId,
                tenant.name,
                w.branchId,
                branch.name,
                w.id,
                w.name,
                wc.dailyConsumed,
                wc.monthlyConsumed,
                w.dailyLimit,
                w.monthlyLimit,
                wc.lastProcessedOccurredAt,
                w.active
            )
            from WalletConsumption wc
            join wc.wallet w
            join Tenant tenant
                on tenant.id = w.tenantId
            left join Branch branch
                on branch.id = w.branchId
               and branch.tenantId = w.tenantId
            where w.tenantId = :tenantId
              and w.id in :walletIds
              and w.id = coalesce(:walletId, w.id)
              and w.branchId = coalesce(:branchId, w.branchId)
              and w.active = coalesce(:active, w.active)
            order by w.name asc, w.id asc
            """)
    List<WalletConsumptionReportReadModel> findReportByTenantIdAndWalletIdIn(@Param("tenantId") UUID tenantId,
                                                                              @Param("walletIds") Collection<UUID> walletIds,
                                                                              @Param("walletId") @Nullable UUID walletId,
                                                                              @Param("branchId") @Nullable UUID branchId,
                                                                              @Param("active") @Nullable Boolean active);

    @Modifying
    @Query("""
            update WalletConsumption c
            set c.dailyConsumed = 0,
                c.dailyWindowDate = :today
            where c.dailyWindowDate <> :today
            """)
    int resetDailyConsumption(@Param("today") java.time.LocalDate today);

    @Modifying
    @Query("""
            update WalletConsumption c
            set c.monthlyConsumed = 0,
                c.monthlyWindowKey = :monthKey
            where c.monthlyWindowKey <> :monthKey
            """)
    int resetMonthlyConsumption(@Param("monthKey") String monthKey);
}
