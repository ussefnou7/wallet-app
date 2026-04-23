package com.wallet.walletapp.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTenantId(UUID tenantId);

    Optional<Transaction> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Transaction> findByTenantIdAndExternalTransactionId(UUID tenantId, String externalTransactionId);

    default Page<Transaction> findAllByFilters(UUID tenantId,
                                               @Nullable UUID walletId,
                                               @Nullable TransactionType type,
                                               @Nullable LocalDateTime fromDate,
                                               @Nullable LocalDateTime toDate,
                                               Pageable pageable) {
        return findAll(TransactionSpecifications.byFilters(tenantId, walletId, type, fromDate, toDate), pageable);
    }

    default Page<Transaction> findAllByFilters(UUID tenantId,
                                               List<UUID> walletIds,
                                               @Nullable TransactionType type,
                                               @Nullable LocalDateTime fromDate,
                                               @Nullable LocalDateTime toDate,
                                               Pageable pageable) {
        return findAll(TransactionSpecifications.byFilters(tenantId, walletIds, type, fromDate, toDate), pageable);
    }

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.tenantId = :tenantId AND t.walletId = :walletId AND t.type = :type")
    BigDecimal sumAmountByWalletAndType(@Param("tenantId") UUID tenantId,
                                        @Param("walletId") UUID walletId,
                                        @Param("type") TransactionType type);

    @Query("SELECT COALESCE(SUM(t.percent), 0) FROM Transaction t WHERE t.tenantId = :tenantId")
    BigDecimal sumFeeByTenant(@Param("tenantId") UUID tenantId);

    @Query("""
            SELECT COALESCE(SUM(t.amount), 0)
            FROM Transaction t
            WHERE t.walletId = :walletId
              AND t.type = com.wallet.walletapp.transaction.TransactionType.DEBIT
              AND t.occurredAt >= :windowStart
              AND t.occurredAt < :windowEnd
            """)
    BigDecimal sumDebitsByWalletIdAndOccurredAtBetween(@Param("walletId") UUID walletId,
                                                       @Param("windowStart") LocalDateTime windowStart,
                                                       @Param("windowEnd") LocalDateTime windowEnd);

    @Query("""
            SELECT
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT THEN t.amount ELSE 0 END), 0),
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.DEBIT THEN t.amount ELSE 0 END), 0),
              COUNT(t)
            FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND (:walletId IS NULL OR t.walletId = :walletId)
              AND (:fromDate IS NULL OR t.occurredAt >= :fromDate)
              AND (:toDate IS NULL OR t.occurredAt <= :toDate)
            """)
    Object[] getSummary(@Param("tenantId") UUID tenantId,
                        @Param("walletId") @Nullable UUID walletId,
                        @Param("fromDate") @Nullable LocalDateTime fromDate,
                        @Param("toDate") @Nullable LocalDateTime toDate);
}
