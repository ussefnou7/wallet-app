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
            select b.id as branchId, b.name as name, b.active as active, t.name as tenantName
            from Branch b
            join Tenant t on t.id = b.tenantId
            order by b.tenantId, b.id
            """)
    List<BranchReadProjection> findAllForRead();

    @Query("""
            select b.id as branchId, b.name as name, b.active as active, t.name as tenantName
            from Branch b
            join Tenant t on t.id = b.tenantId
            where b.id = :id
            """)
    Optional<BranchReadProjection> findReadById(@Param("id") UUID id);

    @Query("""
            select b.id as branchId, b.name as name, b.active as active, t.name as tenantName
            from Branch b
            join Tenant t on t.id = b.tenantId
            order by b.tenantId, b.id
            """)
    Page<BranchReadProjection> findAllForRead(Pageable pageable);
}
