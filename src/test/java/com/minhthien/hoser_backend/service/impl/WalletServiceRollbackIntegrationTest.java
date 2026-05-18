package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.entity.Wallet;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.repository.WalletRepository;
import com.minhthien.hoser_backend.repository.WalletTransactionRepository;
import com.minhthien.hoser_backend.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wallet-rollback-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class WalletServiceRollbackIntegrationTest {

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        walletTransactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();
        walletService.getOrCreateAdminWallet();
    }

    @Test
    void walletBalanceRollsBackWhenLedgerWriteFails() {
        User user = userRepository.save(User.builder()
                .username("rollback-user")
                .email("rollback-user@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.USER)
                .active(true)
                .build());
        walletService.credit(user.getId(), new BigDecimal("100.00"), WalletTransactionType.DEPOSIT,
                "TEST", "ok", "rollback-ok", null, null);

        assertThatThrownBy(() -> walletService.credit(user.getId(), new BigDecimal("10.00"), null,
                "TEST", "fail", "rollback-fail", null, null))
                .isInstanceOf(RuntimeException.class);

        Wallet wallet = walletRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(wallet.getAvailableBalance()).isEqualByComparingTo("100.00");
        assertThat(walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId())).hasSize(1);
    }
}
