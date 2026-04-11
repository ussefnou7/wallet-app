package com.wallet.walletapp.user;

import com.wallet.walletapp.common.BaseEntity;
import com.wallet.walletapp.common.TenantAwareEntity;
import com.wallet.walletapp.tenant.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends TenantAwareEntity {

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    // 👇 ADD THIS (optional but important)
    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;
}