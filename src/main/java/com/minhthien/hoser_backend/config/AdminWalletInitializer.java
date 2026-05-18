package com.minhthien.hoser_backend.config;

import com.minhthien.hoser_backend.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminWalletInitializer implements ApplicationRunner {

    private final WalletService walletService;

    @Override
    public void run(ApplicationArguments args) {
        walletService.getOrCreateAdminWallet();
    }
}
