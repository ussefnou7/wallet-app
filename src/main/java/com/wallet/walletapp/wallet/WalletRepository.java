package com.wallet.walletapp.wallet;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            order by w.tenantId, w.id
            """)
    List<WalletReadProjection> findAllForRead();

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.tenantId = :tenantId
            order by w.tenantId, w.id
            """)
    List<WalletReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId);

    @EntityGraph(attributePaths = "consumption")
    Optional<Wallet> findById(UUID id);

    @EntityGraph(attributePaths = "consumption")
    Optional<Wallet> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.id = :id
            """)
    Optional<WalletReadProjection> findReadById(@Param("id") UUID id);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.id = :id and w.tenantId = :tenantId
            """)
    Optional<WalletReadProjection> findReadByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    List<Wallet> findByIdInAndTenantId(List<UUID> ids, UUID tenantId);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.tenantId = :tenantId and w.type = :type
            order by w.tenantId, w.id
            """)
    List<WalletReadProjection> findByTenantIdAndTypeForRead(@Param("tenantId") UUID tenantId, @Param("type") WalletType type);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.branchId = :branchId
            order by w.tenantId, w.id
            """)
    List<WalletReadProjection> findByBranchIdForRead(@Param("branchId") UUID branchId);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.branchId = :branchId and w.type = :type
            order by w.tenantId, w.id
            """)
    List<WalletReadProjection> findByBranchIdAndTypeForRead(@Param("branchId") UUID branchId, @Param("type") WalletType type);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            order by w.tenantId, w.id
            """)
    Page<WalletReadProjection> findAllForRead(Pageable pageable);

    @Query("""
            select
                w.id as id,
                w.tenantId as tenantId,
                t.name as tenantName,
                w.branchId as branchId,
                w.name as name,
                w.number as number,
                w.balance as balance,
                w.walletProfit as walletProfit,
                w.cashProfit as cashProfit,
                w.dailyLimit as dailyLimit,
                w.monthlyLimit as monthlyLimit,
                w.active as active,
                w.createdAt as createdAt,
                w.updatedAt as updatedAt,
                w.type as type
            from Wallet w
            join Tenant t on t.id = w.tenantId
            where w.tenantId = :tenantId
            order by w.tenantId, w.id
            """)
    Page<WalletReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId, Pageable pageable);
}
