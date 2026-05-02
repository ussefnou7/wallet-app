package com.wallet.walletapp.transaction;

import com.wallet.walletapp.reporting.transaction.TransactionReportReadModel;
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


    Optional<Transaction> findByTenantIdAndExternalTransactionId(UUID tenantId, String externalTransactionId);

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            left join User u on u.id = t.createdBy
            where t.walletId = coalesce(:walletId, t.walletId)
              and t.type = coalesce(:type, t.type)
              and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
              and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
            order by t.occurredAt desc, t.createdAt desc
            """)
    List<TransactionReadProjection> findAllForRead(@Param("walletId") @Nullable UUID walletId,
                                                   @Param("type") @Nullable TransactionType type,
                                                   @Param("dateFrom") @Nullable LocalDateTime dateFrom,
                                                   @Param("dateTo") @Nullable LocalDateTime dateTo);

    @Query(value = """
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            left join User u on u.id = t.createdBy
            where t.walletId = coalesce(:walletId, t.walletId)
              and t.type = coalesce(:type, t.type)
              and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
              and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
            order by t.occurredAt desc, t.createdAt desc
            """,
            countQuery = """
                    select count(t)
                    from Transaction t
                    where t.walletId = coalesce(:walletId, t.walletId)
                      and t.type = coalesce(:type, t.type)
                      and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
                      and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
                    """)
    Page<TransactionReadProjection> findAllForRead(@Param("walletId") @Nullable UUID walletId,
                                                   @Param("type") @Nullable TransactionType type,
                                                   @Param("dateFrom") @Nullable LocalDateTime dateFrom,
                                                   @Param("dateTo") @Nullable LocalDateTime dateTo,
                                                   Pageable pageable);

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            left join User u on u.id = t.createdBy
            where t.tenantId = :tenantId
              and t.walletId = coalesce(:walletId, t.walletId)
              and t.type = coalesce(:type, t.type)
              and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
              and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
            order by t.occurredAt desc, t.createdAt desc
            """)
    List<TransactionReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId,
                                                             @Param("walletId") @Nullable UUID walletId,
                                                             @Param("type") @Nullable TransactionType type,
                                                             @Param("dateFrom") @Nullable LocalDateTime dateFrom,
                                                             @Param("dateTo") @Nullable LocalDateTime dateTo);

    @Query(value = """
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            join User u on u.id = t.createdBy
            where t.tenantId = :tenantId
              and (:walletId IS NULL OR t.walletId = :walletId)
              and (:type IS NULL OR t.type = :type)
              and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
              and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
              and (:createdBy is null or t.createdBy = :createdBy)
            order by t.occurredAt desc, t.createdAt desc
            """,
            countQuery = """
                    select count(t)
                    from Transaction t
                    where t.tenantId = :tenantId
                      and t.walletId = coalesce(:walletId, t.walletId)
                      and t.type = coalesce(:type, t.type)
                      and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
                      and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
                      and (:createdBy is null or t.createdBy = :createdBy) 
                      
                    """)
    Page<TransactionReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId,
                                                             @Param("walletId") @Nullable UUID walletId,
                                                             @Param("type") @Nullable TransactionType type,
                                                             @Param("dateFrom") @Nullable LocalDateTime dateFrom,
                                                             @Param("dateTo") @Nullable LocalDateTime dateTo,
                                                             @Param("createdBy") @Nullable UUID createdBy,
                                                             Pageable pageable);

    @Query(value = """
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            join User u on u.id = t.createdBy
            where t.tenantId = :tenantId
              and (:walletId IS NULL OR t.walletId = :walletId)
              and (:type IS NULL OR t.type = :type)
              and (:dateFrom IS NULL OR t.occurredAt >= :dateFrom)
              and (:dateTo IS NULL OR t.occurredAt <= :dateTo)
            order by t.occurredAt desc, t.createdAt desc
            """,
            countQuery = """
                    select count(t)
                    from Transaction t
                    where t.tenantId = :tenantId
                      and t.walletId in :walletIds
                      and t.walletId = coalesce(:walletId, t.walletId)
                      and t.type = coalesce(:type, t.type)
                      and t.occurredAt >= coalesce(:dateFrom, t.occurredAt)
                      and t.occurredAt <= coalesce(:dateTo, t.occurredAt)
                    """)
    Page<TransactionReadProjection> findAllByTenantIdAndWalletIdInForRead(@Param("tenantId") UUID tenantId,
                                                                           @Param("walletIds") List<UUID> walletIds,
                                                                           @Param("walletId") @Nullable UUID walletId,
                                                                           @Param("type") @Nullable TransactionType type,
                                                                           @Param("dateFrom") @Nullable LocalDateTime dateFrom,
                                                                           @Param("dateTo") @Nullable LocalDateTime dateTo,
                                                                           Pageable pageable);

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.walletId as walletId,
                w.name as walletName,
                t.externalTransactionId as externalTransactionId,
                t.amount as amount,
                t.type as type,
                t.percent as percent,
                t.phoneNumber as phoneNumber,
                t.isCash as cash,
                t.description as description,
                t.occurredAt as occurredAt,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.createdBy as createdBy,
                u.username as createdByUsername
            from Transaction t
            join Wallet w on w.id = t.walletId
            left join User u on u.id = t.createdBy
            where t.id = :id and t.tenantId = :tenantId
            """)
    Optional<TransactionReadProjection> findReadByIdAndTenantId(@Param("id") UUID id, @Param("tenantId") UUID tenantId);

    @Query(value = """
            select new com.wallet.walletapp.reporting.transaction.TransactionReportReadModel(
                t.id,
                t.tenantId,
                tenant.name,
                t.walletId,
                w.name,
                t.createdBy,
                createdByUser.username,
                t.amount,
                t.type,
                t.percent,
                t.phoneNumber,
                t.isCash,
                t.description,
                t.occurredAt,
                t.createdAt
            )
            from Transaction t
            join Wallet w
                on w.id = t.walletId
               and w.tenantId = t.tenantId
            join Tenant tenant
                on tenant.id = t.tenantId
            left join User createdByUser
                on createdByUser.id = t.createdBy
               and createdByUser.tenantId = t.tenantId
            where t.tenantId = :tenantId
              and t.walletId = coalesce(:walletId, t.walletId)
              and w.branchId = coalesce(:branchId, w.branchId)
              and t.type = coalesce(:type, t.type)
              and (:filterByCreatedBy = false or t.createdBy = :createdByUserId)
              and t.isCash = coalesce(:cash, t.isCash)
              and t.occurredAt >= coalesce(:fromDate, t.occurredAt)
              and t.occurredAt <= coalesce(:toDate, t.occurredAt)
            order by t.occurredAt desc, t.createdAt desc
            """,
            countQuery = """
                    select count(t)
                    from Transaction t
                    join Wallet w
                        on w.id = t.walletId
                       and w.tenantId = t.tenantId
                    where t.tenantId = :tenantId
                      and t.walletId = coalesce(:walletId, t.walletId)
                      and w.branchId = coalesce(:branchId, w.branchId)
                      and t.type = coalesce(:type, t.type)
                      and (:filterByCreatedBy = false or t.createdBy = :createdByUserId)
                      and t.isCash = coalesce(:cash, t.isCash)
                      and t.occurredAt >= coalesce(:fromDate, t.occurredAt)
                      and t.occurredAt <= coalesce(:toDate, t.occurredAt)
                    """)
    Page<TransactionReportReadModel> findTransactionReportByTenantId(@Param("tenantId") UUID tenantId,
                                                                     @Param("walletId") @Nullable UUID walletId,
                                                                     @Param("branchId") @Nullable UUID branchId,
                                                                     @Param("type") @Nullable TransactionType type,
                                                                     @Param("filterByCreatedBy") boolean filterByCreatedBy,
                                                                     @Param("createdByUserId") @Nullable UUID createdByUserId,
                                                                     @Param("cash") @Nullable Boolean cash,
                                                                     @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                     @Param("toDate") @Nullable LocalDateTime toDate,
                                                                     Pageable pageable);

    @Query(value = """
            select new com.wallet.walletapp.reporting.transaction.TransactionReportReadModel(
                t.id,
                t.tenantId,
                tenant.name,
                t.walletId,
                w.name,
                t.createdBy,
                createdByUser.username,
                t.amount,
                t.type,
                t.percent,
                t.phoneNumber,
                t.isCash,
                t.description,
                t.occurredAt,
                t.createdAt
            )
            from Transaction t
            join Wallet w
                on w.id = t.walletId
               and w.tenantId = t.tenantId
            join Tenant tenant
                on tenant.id = t.tenantId
            left join User createdByUser
                on createdByUser.id = t.createdBy
               and createdByUser.tenantId = t.tenantId
            where t.tenantId = :tenantId
              and t.walletId in :walletIds
              and t.walletId = coalesce(:walletId, t.walletId)
              and w.branchId = coalesce(:branchId, w.branchId)
              and t.type = coalesce(:type, t.type)
              and (:filterByCreatedBy = false or t.createdBy = :createdByUserId)
              and t.isCash = coalesce(:cash, t.isCash)
              and t.occurredAt >= coalesce(:fromDate, t.occurredAt)
              and t.occurredAt <= coalesce(:toDate, t.occurredAt)
            order by t.occurredAt desc, t.createdAt desc
            """,
            countQuery = """
                    select count(t)
                    from Transaction t
                    join Wallet w
                        on w.id = t.walletId
                       and w.tenantId = t.tenantId
                    where t.tenantId = :tenantId
                      and t.walletId in :walletIds
                      and t.walletId = coalesce(:walletId, t.walletId)
                      and w.branchId = coalesce(:branchId, w.branchId)
                      and t.type = coalesce(:type, t.type)
                      and (:filterByCreatedBy = false or t.createdBy = :createdByUserId)
                      and t.isCash = coalesce(:cash, t.isCash)
                      and t.occurredAt >= coalesce(:fromDate, t.occurredAt)
                      and t.occurredAt <= coalesce(:toDate, t.occurredAt)
                    """)
    Page<TransactionReportReadModel> findTransactionReportByTenantIdAndWalletIdIn(@Param("tenantId") UUID tenantId,
                                                                                   @Param("walletIds") List<UUID> walletIds,
                                                                                   @Param("walletId") @Nullable UUID walletId,
                                                                                   @Param("branchId") @Nullable UUID branchId,
                                                                                   @Param("type") @Nullable TransactionType type,
                                                                                   @Param("filterByCreatedBy") boolean filterByCreatedBy,
                                                                                   @Param("createdByUserId") @Nullable UUID createdByUserId,
                                                                                   @Param("cash") @Nullable Boolean cash,
                                                                                   @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                                   @Param("toDate") @Nullable LocalDateTime toDate,
                                                                                   Pageable pageable);

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

    @Query(value = """
            SELECT
              TO_CHAR(DATE_TRUNC('day', t.occurred_at), 'YYYY-MM-DD') AS "period",
              COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0) AS "totalCredits",
              COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0) AS "totalDebits",
              COUNT(t.id) AS "transactionCount"
            FROM transactions t
            WHERE t.tenant_id = :tenantId
              AND t.wallet_id = COALESCE(:walletId, t.wallet_id)
              AND t.occurred_at >= COALESCE(:fromDate, t.occurred_at)
              AND t.occurred_at <= COALESCE(:toDate, t.occurred_at)
            GROUP BY DATE_TRUNC('day', t.occurred_at)
            ORDER BY DATE_TRUNC('day', t.occurred_at)
            """, nativeQuery = true)
    List<TransactionTimeAggregationProjection> getDailyTimeAggregation(@Param("tenantId") UUID tenantId,
                                                                       @Param("walletId") @Nullable UUID walletId,
                                                                       @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                       @Param("toDate") @Nullable LocalDateTime toDate);

    @Query(value = """
            SELECT
              TO_CHAR(DATE_TRUNC('month', t.occurred_at), 'YYYY-MM') AS "period",
              COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0) AS "totalCredits",
              COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0) AS "totalDebits",
              COUNT(t.id) AS "transactionCount"
            FROM transactions t
            WHERE t.tenant_id = :tenantId
              AND t.wallet_id = COALESCE(:walletId, t.wallet_id)
              AND t.occurred_at >= COALESCE(:fromDate, t.occurred_at)
              AND t.occurred_at <= COALESCE(:toDate, t.occurred_at)
            GROUP BY DATE_TRUNC('month', t.occurred_at)
            ORDER BY DATE_TRUNC('month', t.occurred_at)
            """, nativeQuery = true)
    List<TransactionTimeAggregationProjection> getMonthlyTimeAggregation(@Param("tenantId") UUID tenantId,
                                                                         @Param("walletId") @Nullable UUID walletId,
                                                                         @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                         @Param("toDate") @Nullable LocalDateTime toDate);

    @Query(value = """
            SELECT
              TO_CHAR(DATE_TRUNC('day', t.occurred_at), 'YYYY-MM-DD') AS "period",
              COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0) AS "totalCredits",
              COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0) AS "totalDebits",
              COUNT(t.id) AS "transactionCount"
            FROM transactions t
            WHERE t.tenant_id = :tenantId
              AND t.wallet_id IN (:walletIds)
              AND t.occurred_at >= COALESCE(:fromDate, t.occurred_at)
              AND t.occurred_at <= COALESCE(:toDate, t.occurred_at)
            GROUP BY DATE_TRUNC('day', t.occurred_at)
            ORDER BY DATE_TRUNC('day', t.occurred_at)
            """, nativeQuery = true)
    List<TransactionTimeAggregationProjection> getDailyTimeAggregationForWallets(@Param("tenantId") UUID tenantId,
                                                                                 @Param("walletIds") List<UUID> walletIds,
                                                                                 @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                                 @Param("toDate") @Nullable LocalDateTime toDate);

    @Query(value = """
            SELECT
              TO_CHAR(DATE_TRUNC('month', t.occurred_at), 'YYYY-MM') AS "period",
              COALESCE(SUM(CASE WHEN t.type = 'CREDIT' THEN t.amount ELSE 0 END), 0) AS "totalCredits",
              COALESCE(SUM(CASE WHEN t.type = 'DEBIT' THEN t.amount ELSE 0 END), 0) AS "totalDebits",
              COUNT(t.id) AS "transactionCount"
            FROM transactions t
            WHERE t.tenant_id = :tenantId
              AND t.wallet_id IN (:walletIds)
              AND t.occurred_at >= COALESCE(:fromDate, t.occurred_at)
              AND t.occurred_at <= COALESCE(:toDate, t.occurred_at)
            GROUP BY DATE_TRUNC('month', t.occurred_at)
            ORDER BY DATE_TRUNC('month', t.occurred_at)
            """, nativeQuery = true)
    List<TransactionTimeAggregationProjection> getMonthlyTimeAggregationForWallets(@Param("tenantId") UUID tenantId,
                                                                                   @Param("walletIds") List<UUID> walletIds,
                                                                                   @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                                                   @Param("toDate") @Nullable LocalDateTime toDate);

    @Query("""
            SELECT
              COALESCE(SUM(CASE
                  WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT AND t.isCash = true
                      THEN 0
                  ELSE t.percent
              END), 0) as totalWalletProfit,
              COALESCE(SUM(CASE
                  WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT AND t.isCash = true
                      THEN t.percent
                  ELSE 0
              END), 0) as totalCashProfit
            FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND t.walletId = COALESCE(:walletId, t.walletId)
              AND t.occurredAt >= COALESCE(:fromDate, t.occurredAt)
              AND t.occurredAt <= COALESCE(:toDate, t.occurredAt)
            """)
    ProfitSummaryProjection getProfitSummary(@Param("tenantId") UUID tenantId,
                                             @Param("walletId") @Nullable UUID walletId,
                                             @Param("fromDate") @Nullable LocalDateTime fromDate,
                                             @Param("toDate") @Nullable LocalDateTime toDate);

    @Query("""
            SELECT
              COALESCE(SUM(CASE
                  WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT AND t.isCash = true
                      THEN 0
                  ELSE t.percent
              END), 0) as totalWalletProfit,
              COALESCE(SUM(CASE
                  WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT AND t.isCash = true
                      THEN t.percent
                  ELSE 0
              END), 0) as totalCashProfit
            FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND t.walletId IN :walletIds
              AND t.occurredAt >= COALESCE(:fromDate, t.occurredAt)
              AND t.occurredAt <= COALESCE(:toDate, t.occurredAt)
            """)
    ProfitSummaryProjection getProfitSummaryForWallets(@Param("tenantId") UUID tenantId,
                                                       @Param("walletIds") List<UUID> walletIds,
                                                       @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                       @Param("toDate") @Nullable LocalDateTime toDate);

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
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT THEN t.amount ELSE 0 END), 0) as totalCredits,
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.DEBIT THEN t.amount ELSE 0 END), 0) as totalDebits,
              COUNT(t) as transactionCount
            FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND t.walletId = COALESCE(:walletId, t.walletId)
              AND t.occurredAt >= COALESCE(:fromDate, t.occurredAt)
              AND t.occurredAt <= COALESCE(:toDate, t.occurredAt)
            """)
    TransactionSummaryProjection getSummary(@Param("tenantId") UUID tenantId,
                                            @Param("walletId") @Nullable UUID walletId,
                                            @Param("fromDate") @Nullable LocalDateTime fromDate,
                                            @Param("toDate") @Nullable LocalDateTime toDate);

    @Query("""
            SELECT
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.CREDIT THEN t.amount ELSE 0 END), 0) as totalCredits,
              COALESCE(SUM(CASE WHEN t.type = com.wallet.walletapp.transaction.TransactionType.DEBIT THEN t.amount ELSE 0 END), 0) as totalDebits,
              COUNT(t) as transactionCount
            FROM Transaction t
            WHERE t.tenantId = :tenantId
              AND t.walletId IN :walletIds
              AND t.occurredAt >= COALESCE(:fromDate, t.occurredAt)
              AND t.occurredAt <= COALESCE(:toDate, t.occurredAt)
            """)
    TransactionSummaryProjection getSummaryForWallets(@Param("tenantId") UUID tenantId,
                                                      @Param("walletIds") List<UUID> walletIds,
                                                      @Param("fromDate") @Nullable LocalDateTime fromDate,
                                                      @Param("toDate") @Nullable LocalDateTime toDate);
}
