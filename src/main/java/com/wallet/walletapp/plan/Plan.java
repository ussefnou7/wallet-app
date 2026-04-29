package com.wallet.walletapp.plan;

import com.wallet.walletapp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer maxUsers;

    @Column(nullable = false)
    private Integer maxWallets;

    @Column(nullable = false)
    private Integer maxBranches;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;
}
