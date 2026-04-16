package com.wallet.walletapp.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletConsumptionResetScheduler {

    private final WalletConsumptionService walletConsumptionService;

    // Runs after midnight to clear the previous day's consumption window.
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyConsumption() {
        walletConsumptionService.resetDailyConsumption();
    }

    // Runs on the first day of each month to clear the previous month's consumption window.
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyConsumption() {
        walletConsumptionService.resetMonthlyConsumption();
    }
}
