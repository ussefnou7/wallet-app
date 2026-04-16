package com.wallet.walletapp.wallet;

import com.wallet.walletapp.transaction.Transaction;
import com.wallet.walletapp.transaction.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletConsumptionService {

    private static final DateTimeFormatter MONTH_KEY = DateTimeFormatter.ofPattern("yyyy-MM");

    private final WalletConsumptionRepository walletConsumptionRepository;

    // Step 1: create one consumption record immediately after a wallet is created.
    @Transactional
    public WalletConsumption createForWallet(Wallet wallet) {
        WalletConsumption consumption = WalletConsumption.builder()
                .walletId(wallet.getId())
                .wallet(wallet)
                .dailyWindowDate(LocalDate.now())
                .monthlyWindowKey(currentMonthKey())
                .dailyConsumed(BigDecimal.ZERO)
                .monthlyConsumed(BigDecimal.ZERO)
                .build();

        WalletConsumption savedConsumption = walletConsumptionRepository.save(consumption);
        wallet.setConsumption(savedConsumption);
        return savedConsumption;
    }

    // Used by wallet reads to load the already-tracked consumption record.
    @Transactional(readOnly = true)
    public WalletConsumption getByWallet(Wallet wallet) {
        return walletConsumptionRepository.findByWalletId(wallet.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet consumption not found"));
    }

    // Step 2: update daily/monthly counters synchronously when a transaction is created.
    @Transactional
    public void applyTransaction(Wallet wallet, Transaction transaction) {
        WalletConsumption consumption = walletConsumptionRepository.findByWalletId(wallet.getId())
                .orElseThrow(() -> new IllegalStateException("Wallet consumption not found"));

        if (transaction.getType() == TransactionType.DEBIT) {
            if (LocalDate.now().equals(transaction.getOccurredAt().toLocalDate())) {
                consumption.setDailyConsumed(consumption.getDailyConsumed().add(transaction.getAmount()));
            }
            if (YearMonth.now().equals(YearMonth.from(transaction.getOccurredAt()))) {
                consumption.setMonthlyConsumed(consumption.getMonthlyConsumed().add(transaction.getAmount()));
            }
        }

        consumption.setLastProcessedTransactionId(transaction.getId());
        consumption.setLastProcessedOccurredAt(transaction.getOccurredAt());
        wallet.setConsumption(walletConsumptionRepository.save(consumption));
    }

    // Step 3a: async daily reset job sets all records to the new daily window.
    @Transactional
    public void resetDailyConsumption() {
        LocalDate today = LocalDate.now();
        int updated = walletConsumptionRepository.resetDailyConsumption(today);
        if (updated > 0) {
            log.info("Reset daily wallet consumption for {} wallet(s)", updated);
        }
    }

    // Step 3b: async monthly reset job sets all records to the new monthly window.
    @Transactional
    public void resetMonthlyConsumption() {
        String monthKey = currentMonthKey();
        int updated = walletConsumptionRepository.resetMonthlyConsumption(monthKey);
        if (updated > 0) {
            log.info("Reset monthly wallet consumption for {} wallet(s)", updated);
        }
    }

    private String currentMonthKey() {
        return YearMonth.now().format(MONTH_KEY);
    }
}
