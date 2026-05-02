package com.wallet.walletapp.wallet.profit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfitCollectionRepository extends JpaRepository<ProfitCollection, UUID> {
}
