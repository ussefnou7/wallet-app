package com.wallet.walletapp.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    List<User> findByTenantId(UUID tenantId);
    List<User> findByRole(Role role);
    List<User> findByTenantIdAndRole(UUID tenantId, Role role);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            order by u.tenantId, u.id
            """)
    List<UserReadProjection> findAllForRead();

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            where u.id = :id
            """)
    Optional<UserReadProjection> findReadById(@Param("id") UUID id);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            where u.tenantId = :tenantId
            order by u.tenantId, u.id
            """)
    List<UserReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            where u.role = :role
            order by u.tenantId, u.id
            """)
    List<UserReadProjection> findAllByRoleForRead(@Param("role") Role role);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            where u.tenantId = :tenantId and u.role = :role
            order by u.tenantId, u.id
            """)
    List<UserReadProjection> findAllByTenantIdAndRoleForRead(@Param("tenantId") UUID tenantId, @Param("role") Role role);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            order by u.tenantId, u.id
            """)
    Page<UserReadProjection> findAllForRead(Pageable pageable);

    @Query("""
            select u.id as id, u.username as username, u.role as role, t.name as tenantName
            from User u
            join Tenant t on t.id = u.tenantId
            where u.tenantId = :tenantId
            order by u.tenantId, u.id
            """)
    Page<UserReadProjection> findAllByTenantIdForRead(@Param("tenantId") UUID tenantId, Pageable pageable);

    boolean existsByUsername(String username);

    long countByTenantId(UUID tenantId);
}
