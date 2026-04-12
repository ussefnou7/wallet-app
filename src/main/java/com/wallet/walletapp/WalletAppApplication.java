package com.wallet.walletapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WalletAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(WalletAppApplication.class, args);
    }

}
