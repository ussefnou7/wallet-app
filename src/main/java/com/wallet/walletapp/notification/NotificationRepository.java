package com.wallet.walletapp.notification;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Modifying(flushAutomatically = true)
    @Query(value = """
            insert into notifications (
                id,
                tenant_id,
                recipient_user_id,
                type,
                priority,
                priority_rank,
                severity,
                title_key,
                message_key,
                payload_json,
                target_type,
                target_id,
                idempotency_key,
                read_at,
                created_at,
                updated_at
            ) values (
                :id,
                :tenantId,
                :recipientUserId,
                :type,
                :priority,
                :priorityRank,
                :severity,
                :titleKey,
                :messageKey,
                cast(:payloadJson as jsonb),
                :targetType,
                :targetId,
                :idempotencyKey,
                :readAt,
                :createdAt,
                :updatedAt
            )
            on conflict (idempotency_key)
            where idempotency_key is not null
            do nothing
            """, nativeQuery = true)
    int insertIgnoreIdempotencyConflict(@Param("id") UUID id,
                                        @Param("tenantId") UUID tenantId,
                                        @Param("recipientUserId") UUID recipientUserId,
                                        @Param("type") String type,
                                        @Param("priority") String priority,
                                        @Param("priorityRank") int priorityRank,
                                        @Param("severity") String severity,
                                        @Param("titleKey") String titleKey,
                                        @Param("messageKey") String messageKey,
                                        @Param("payloadJson") String payloadJson,
                                        @Param("targetType") String targetType,
                                        @Param("targetId") UUID targetId,
                                        @Param("idempotencyKey") String idempotencyKey,
                                        @Param("readAt") LocalDateTime readAt,
                                        @Param("createdAt") LocalDateTime createdAt,
                                        @Param("updatedAt") LocalDateTime updatedAt);

    List<Notification> findByTenantIdAndRecipientUserIdAndReadAtIsNullOrderByPriorityRankDescCreatedAtDesc(
            UUID tenantId,
            UUID recipientUserId,
            Pageable pageable
    );

    long countByTenantIdAndRecipientUserIdAndReadAtIsNull(UUID tenantId, UUID recipientUserId);

    Optional<Notification> findByIdAndTenantIdAndRecipientUserId(UUID id, UUID tenantId, UUID recipientUserId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
            set n.readAt = :readAt,
                n.updatedAt = :readAt
            where n.tenantId = :tenantId
              and n.recipientUserId = :recipientUserId
              and n.readAt is null
              and n.priority = :priority
            """)
    int markUnreadByPriorityAsRead(@Param("tenantId") UUID tenantId,
                                   @Param("recipientUserId") UUID recipientUserId,
                                   @Param("priority") NotificationPriority priority,
                                   @Param("readAt") LocalDateTime readAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Notification n
            set n.readAt = :readAt,
                n.updatedAt = :readAt
            where n.tenantId = :tenantId
              and n.recipientUserId = :recipientUserId
              and n.readAt is null
            """)
    int markAllUnreadAsRead(@Param("tenantId") UUID tenantId,
                            @Param("recipientUserId") UUID recipientUserId,
                            @Param("readAt") LocalDateTime readAt);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            delete from Notification n
            where n.readAt is not null
              and n.readAt < :threshold
            """)
    int deleteReadOlderThan(@Param("threshold") LocalDateTime threshold);
}
