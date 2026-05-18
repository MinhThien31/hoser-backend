package com.minhthien.hoser_backend.config;

import com.minhthien.hoser_backend.service.WalletService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AdminWalletInitializerTest {

    @Test
    void runEnsuresAdminWalletExists() {
        WalletService walletService = mock(WalletService.class);
        AdminWalletInitializer initializer = new AdminWalletInitializer(walletService);

        initializer.run(null);

        verify(walletService).getOrCreateAdminWallet();
    }
}
