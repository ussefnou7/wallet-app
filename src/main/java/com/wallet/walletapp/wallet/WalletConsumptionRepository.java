package com.wallet.walletapp.wallet;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletConsumptionRepository extends JpaRepository<WalletConsumption, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletConsumption> findByWalletId(UUID walletId);

    List<WalletConsumption> findAllByWalletIdIn(Collection<UUID> walletIds);

    @Modifying
    @Query("""
            update WalletConsumption c
            set c.dailyConsumed = 0,
                c.dailyWindowDate = :today
            where c.dailyWindowDate <> :today
            """)
    int resetDailyConsumption(@Param("today") java.time.LocalDate today);

    @Modifying
    @Query("""
            update WalletConsumption c
            set c.monthlyConsumed = 0,
                c.monthlyWindowKey = :monthKey
            where c.monthlyWindowKey <> :monthKey
            """)
    int resetMonthlyConsumption(@Param("monthKey") String monthKey);
}
