package com.wallet.walletapp.support;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.createdBy as createdBy,
                t.subject as subject,
                t.description as description,
                t.priority as priority,
                t.status as status,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.resolvedAt as resolvedAt,
                t.resolvedBy as resolvedBy,
                tenant.name as tenantName,
                creator.username as createdByName
            from SupportTicket t
                   join Tenant tenant on tenant.id = t.tenantId
                   join User creator on creator.id = t.createdBy
            where t.tenantId = :tenantId
            order by t.createdAt desc
            """)
    List<SupportTicketReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId);

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.createdBy as createdBy,
                t.subject as subject,
                t.description as description,
                t.priority as priority,
                t.status as status,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.resolvedAt as resolvedAt,
                t.resolvedBy as resolvedBy,
                tenant.name as tenantName,
                creator.username as createdByName
            from SupportTicket t
                   join Tenant tenant on tenant.id = t.tenantId
                   join User creator on creator.id = t.createdBy
            where t.id = :ticketId
              and t.tenantId = :tenantId
            """)
    Optional<SupportTicketReadProjection> findByIdAndTenantIdForRead(@Param("ticketId") UUID ticketId,
                                                                     @Param("tenantId") UUID tenantId);

    @Query("""
            select
                t.id as id,
                t.tenantId as tenantId,
                t.createdBy as createdBy,
                t.subject as subject,
                t.description as description,
                t.priority as priority,
                t.status as status,
                t.createdAt as createdAt,
                t.updatedAt as updatedAt,
                t.resolvedAt as resolvedAt,
                t.resolvedBy as resolvedBy,
                tenant.name as tenantName,
                creator.username as createdByName
            from SupportTicket t
                   join Tenant tenant on tenant.id = t.tenantId
                   join User creator on creator.id = t.createdBy
            where t.tenantId = coalesce(:tenantId, t.tenantId)
              and t.status = coalesce(:status, t.status)
              and t.priority = coalesce(:priority, t.priority)
            order by t.createdAt desc
            """)
    List<SupportTicketReadProjection> findAllForAdmin(@Param("tenantId") @Nullable UUID tenantId,
                                                      @Param("status") @Nullable SupportTicketStatus status,
                                                      @Param("priority") @Nullable SupportTicketPriority priority);
}
