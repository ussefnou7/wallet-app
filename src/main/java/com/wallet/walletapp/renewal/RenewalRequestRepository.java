package com.wallet.walletapp.renewal;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RenewalRequestRepository extends JpaRepository<RenewalRequest, UUID> {

    @Query("""
            select
                r.id as id,
                r.tenantId as tenantId,
                r.requestedBy as requestedBy,
                r.phoneNumber as phoneNumber,
                r.amount as amount,
                r.periodMonths as periodMonths,
                r.status as status,
                r.reviewedAt as reviewedAt,
                r.reviewedBy as reviewedBy,
                r.adminNote as adminNote,
                r.createdAt as createdAt,
                r.updatedAt as updatedAt
            from RenewalRequest r
            where r.tenantId = :tenantId
            order by r.createdAt desc
            """)
    List<RenewalRequestReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId);

    @Query("""
            select
                r.id as id,
                r.tenantId as tenantId,
                r.requestedBy as requestedBy,
                r.phoneNumber as phoneNumber,
                r.amount as amount,
                r.periodMonths as periodMonths,
                r.status as status,
                r.reviewedAt as reviewedAt,
                r.reviewedBy as reviewedBy,
                r.adminNote as adminNote,
                r.createdAt as createdAt,
                r.updatedAt as updatedAt
            from RenewalRequest r
            where r.id = :requestId
              and r.tenantId = :tenantId
            """)
    Optional<RenewalRequestReadProjection> findByIdAndTenantIdForRead(@Param("requestId") UUID requestId,
                                                                      @Param("tenantId") UUID tenantId);

    @Query("""
            select
                r.id as id,
                r.tenantId as tenantId,
                r.requestedBy as requestedBy,
                r.phoneNumber as phoneNumber,
                r.amount as amount,
                r.periodMonths as periodMonths,
                r.status as status,
                r.reviewedAt as reviewedAt,
                r.reviewedBy as reviewedBy,
                r.adminNote as adminNote,
                r.createdAt as createdAt,
                r.updatedAt as updatedAt
            from RenewalRequest r
            where r.tenantId = coalesce(:tenantId, r.tenantId)
              and r.status = coalesce(:status, r.status)
            order by r.createdAt desc
            """)
    List<RenewalRequestReadProjection> findAllForAdmin(@Param("tenantId") @Nullable UUID tenantId,
                                                       @Param("status") @Nullable RenewalRequestStatus status);
}
