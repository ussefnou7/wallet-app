package com.wallet.walletapp.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletConsumptionResetScheduler {

    private final WalletConsumptionService walletConsumptionService;

    // Temporary test trigger: runs once at 2026-04-16 19:53 local time.
    @Scheduled(cron = "0 53 19 16 4 *")
    public void resetDailyConsumption() {
        walletConsumptionService.resetDailyConsumption();
    }

    // Temporary test trigger: runs once at 2026-04-16 19:53 local time.
    @Scheduled(cron = "0 53 19 16 4 *")
    public void resetMonthlyConsumption() {
        walletConsumptionService.resetMonthlyConsumption();
    }
}
