package com.wallet.walletapp.wallet.profit.dto;

import com.wallet.walletapp.wallet.profit.ProfitCollection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProfitCollectionRepository extends JpaRepository<ProfitCollection, UUID> {
}
