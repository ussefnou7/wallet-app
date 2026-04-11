package com.wallet.walletapp.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByTenantId(UUID tenantId);

    Optional<Transaction> findByIdAndTenantId(UUID id, UUID tenantId);

    @Query("""
            SELECT t FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND (:walletId IS NULL OR t.walletId = :walletId)
              AND (:type     IS NULL OR t.type     = :type)
              AND (:dateFrom IS NULL OR t.createdAt >= :dateFrom)
              AND (:dateTo   IS NULL OR t.createdAt <= :dateTo)
            ORDER BY t.createdAt DESC
            """)
    List<Transaction> findByFilters(
            @Param("tenantId") UUID tenantId,
            @Param("walletId") UUID walletId,
            @Param("type") TransactionType type,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo
    );

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.tenantId = :tenantId AND t.walletId = :walletId AND t.type = :type")
    BigDecimal sumAmountByWalletAndType(@Param("tenantId") UUID tenantId,
                                        @Param("walletId") UUID walletId,
                                        @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.percent), 0) FROM Transaction t WHERE t.tenantId = :tenantId")
    BigDecimal sumFeeByTenant(@Param("tenantId") UUID tenantId);
}
