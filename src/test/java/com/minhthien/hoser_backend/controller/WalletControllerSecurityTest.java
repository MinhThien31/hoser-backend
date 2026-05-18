package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.enums.WalletTransactionType;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.repository.WalletRepository;
import com.minhthien.hoser_backend.repository.WalletTransactionRepository;
import com.minhthien.hoser_backend.security.JwtTokenProvider;
import com.minhthien.hoser_backend.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:wallet-controller-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class WalletControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private WalletService walletService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
    void userCanGetOwnWallet() throws Exception {
        User user = createUser("wallet-user", "wallet-user@example.com", UserRole.USER);
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        mockMvc.perform(get("/api/v1/wallets/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.userId", is(user.getId().intValue())))
                .andExpect(jsonPath("$.data.currency", is("VND")))
                .andExpect(jsonPath("$.data.availableBalance", is(0)))
                .andExpect(jsonPath("$.data.holdBalance", is(0)));
    }

    @Test
    void userOnlyGetsOwnWalletTransactions() throws Exception {
        User user = createUser("tx-user", "tx-user@example.com", UserRole.USER);
        User otherUser = createUser("other-user", "other-user@example.com", UserRole.USER);
        walletService.credit(user.getId(), new BigDecimal("10.00"), WalletTransactionType.DEPOSIT,
                "TEST", "1", "user-tx", null, null);
        walletService.credit(otherUser.getId(), new BigDecimal("20.00"), WalletTransactionType.DEPOSIT,
                "TEST", "2", "other-tx", null, null);
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        mockMvc.perform(get("/api/v1/wallets/me/transactions")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].userId", is(user.getId().intValue())))
                .andExpect(jsonPath("$.data[0].amount", is(10.00)));
    }

    @Test
    void normalUserCannotGetAdminWallet() throws Exception {
        User user = createUser("not-admin", "not-admin@example.com", UserRole.USER);
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());

        mockMvc.perform(get("/api/v1/admin/wallet")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Access denied")));
    }

    @Test
    void anonymousUserGetsConsistentUnauthorizedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/wallets/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Unauthorized")));
    }

    @Test
    void validationErrorsUseConsistentResponseFormat() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "ab",
                                  "email": "not-an-email",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Validation failed")))
                .andExpect(jsonPath("$.data.username").exists())
                .andExpect(jsonPath("$.data.email").exists())
                .andExpect(jsonPath("$.data.password").exists())
                .andExpect(jsonPath("$.message", not(containsString("Exception"))));
    }

    @Test
    void adminCanGetAdminWallet() throws Exception {
        User admin = createUser("admin", "admin@example.com", UserRole.ADMIN);
        String token = jwtTokenProvider.generateTokenFromUsername(admin.getUsername());

        mockMvc.perform(get("/api/v1/admin/wallet")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.ownerType", is("ADMIN")))
                .andExpect(jsonPath("$.data.userId").doesNotExist())
                .andExpect(jsonPath("$.data.currency", is("VND")));
    }

    private User createUser(String username, String email, UserRole role) {
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode("password123"))
                .role(role)
                .active(true)
                .build();
        return userRepository.save(user);
    }
}
