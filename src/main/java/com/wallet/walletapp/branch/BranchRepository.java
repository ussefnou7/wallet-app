package com.wallet.walletapp.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
    Branch findByTenantIdAndId(UUID tenantId, UUID id);
    List<Branch> findByTenantId(UUID tenantId);

    @Query("""
            select
                b.id as branchId,
                b.name as name,
                b.active as active,
                t.name as tenantName,
                (
                    select count(bu.id)
                    from BranchUser bu
                    where bu.branchId = b.id and bu.tenantId = b.tenantId
                ) as userCount,
                (
                    select count(w.id)
                    from Wallet w
                    where w.branchId = b.id and w.tenantId = b.tenantId
                ) as walletCount
            from Branch b
            join Tenant t on t.id = b.tenantId
            order by b.tenantId, b.id
            """)
    List<BranchReadProjection> findAllForRead();

    @Query("""
            select
                b.id as branchId,
                b.name as name,
                b.active as active,
                t.name as tenantName,
                (
                    select count(bu.id)
                    from BranchUser bu
                    where bu.branchId = b.id and bu.tenantId = b.tenantId
                ) as userCount,
                (
                    select count(w.id)
                    from Wallet w
                    where w.branchId = b.id and w.tenantId = b.tenantId
                ) as walletCount
            from Branch b
            join Tenant t on t.id = b.tenantId
            where b.tenantId = :tenantId
            order by b.tenantId, b.id
            """)
    List<BranchReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId);

    @Query("""
            select
                b.id as branchId,
                b.name as name,
                b.active as active,
                t.name as tenantName,
                (
                    select count(bu.id)
                    from BranchUser bu
                    where bu.branchId = b.id and bu.tenantId = b.tenantId
                ) as userCount,
                (
                    select count(w.id)
                    from Wallet w
                    where w.branchId = b.id and w.tenantId = b.tenantId
                ) as walletCount
            from Branch b
            join Tenant t on t.id = b.tenantId
            where b.id = :id
            """)
    Optional<BranchReadProjection> findReadById(@Param("id") UUID id);

    @Query(value = """
            select
                b.id as branchId,
                b.name as name,
                b.active as active,
                t.name as tenantName,
                (
                    select count(bu.id)
                    from BranchUser bu
                    where bu.branchId = b.id and bu.tenantId = b.tenantId
                ) as userCount,
                (
                    select count(w.id)
                    from Wallet w
                    where w.branchId = b.id and w.tenantId = b.tenantId
                ) as walletCount
            from Branch b
            join Tenant t on t.id = b.tenantId
            order by b.tenantId, b.id
            """,
            countQuery = "select count(b) from Branch b")
    Page<BranchReadProjection> findAllForRead(Pageable pageable);

    @Query(value = """
            select
                b.id as branchId,
                b.name as name,
                b.active as active,
                t.name as tenantName,
                (
                    select count(bu.id)
                    from BranchUser bu
                    where bu.branchId = b.id and bu.tenantId = b.tenantId
                ) as userCount,
                (
                    select count(w.id)
                    from Wallet w
                    where w.branchId = b.id and w.tenantId = b.tenantId
                ) as walletCount
            from Branch b
            join Tenant t on t.id = b.tenantId
            where b.tenantId = :tenantId
            order by b.tenantId, b.id
            """,
            countQuery = "select count(b) from Branch b where b.tenantId = :tenantId")
    Page<BranchReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId, Pageable pageable);

    long countByTenantId(UUID tenantId);
}
