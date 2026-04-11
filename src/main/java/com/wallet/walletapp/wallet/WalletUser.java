package com.wallet.walletapp.wallet;

import com.wallet.walletapp.common.TenantAwareEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "wallet_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "wallet_id"}))
public class WalletUser extends TenantAwareEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "wallet_id", nullable = false)
    private UUID walletId;
}