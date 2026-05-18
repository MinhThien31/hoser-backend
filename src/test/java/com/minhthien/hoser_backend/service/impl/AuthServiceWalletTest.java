package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.RegisterRequest;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.repository.PasswordResetOtpRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.security.JwtTokenProvider;
import com.minhthien.hoser_backend.service.MailService;
import com.minhthien.hoser_backend.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceWalletTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetOtpRepository otpRepository;

    @Mock
    private MailService mailService;

    @Mock
    private WalletService walletService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                jwtTokenProvider,
                userRepository,
                passwordEncoder,
                authenticationManager,
                otpRepository,
                mailService,
                walletService
        );
    }

    @Test
    void registerCreatesDefaultUserWallet() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("new-user");
        request.setEmail("new-user@example.com");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setPhone("0900000000");

        when(userRepository.existsByEmail("new-user@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("new-user")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(7L);
            return user;
        });
        when(jwtTokenProvider.generateTokenFromUsername("new-user")).thenReturn("jwt-token");

        authService.register(request);

        verify(walletService).getOrCreateUserWallet(7L);
    }
}
