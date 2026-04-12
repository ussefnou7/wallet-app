package com.wallet.walletapp.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByTenantId(UUID tenantId);

    Optional<Transaction> findByIdAndTenantId(UUID id, UUID tenantId);

    Optional<Transaction> findByTenantIdAndExternalTransactionId(UUID tenantId, String externalTransactionId);

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
}
