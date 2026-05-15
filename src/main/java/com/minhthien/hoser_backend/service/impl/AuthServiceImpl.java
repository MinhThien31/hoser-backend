package com.minhthien.hoser_backend.service.impl;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.minhthien.hoser_backend.dto.request.LoginRequest;
import com.minhthien.hoser_backend.dto.request.RegisterRequest;
import com.minhthien.hoser_backend.dto.response.AuthResponse;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.PasswordResetOtp;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.DuplicateResourceException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.repository.PasswordResetOtpRepository;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.security.JwtTokenProvider;
import com.minhthien.hoser_backend.service.AuthService;
import com.minhthien.hoser_backend.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetOtpRepository otpRepository;
    private final MailService mailService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .fullName(request.getFullName())
                .Phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .active(true)
                .build();

        user = userRepository.save(user);
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        return buildAuthResponse(user, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        ensureDefaultUserRole(user);
        String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
        return buildAuthResponse(user, token);
    }

    @Override
    public UserResponse getCurrentUser(String usernameOrEmail) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> userRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + usernameOrEmail)));

        return mapToUserResponse(user);
    }

    @Override
    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found"));

        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .email(email)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpRepository.save(resetOtp);
        mailService.sendOtp(email, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        PasswordResetOtp resetOtp = otpRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new BadRequestException("Invalid OTP"));

        if (resetOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public AuthResponse loginGoogle(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleToken = verifier.verify(idToken);
            if (googleToken == null) {
                throw new BadRequestException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String picture = (String) payload.get("picture");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(email)
                            .username(name)
                            .avatarUrl(picture)
                            .role(UserRole.USER)
                            .active(true)
                            .build()));
            ensureDefaultUserRole(user);

            String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Google login failed");
        }
    }

    @Override
    public AuthResponse loginFacebook(String accessToken) {
        try {
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture&access_token=" + accessToken;
            RestTemplate restTemplate = new RestTemplate();
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                throw new BadRequestException("Invalid Facebook token");
            }

            String email = (String) response.get("email");
            String name = (String) response.get("name");
            String id = (String) response.get("id");

            if (email == null) {
                email = id + "@facebook.com";
            }

            String finalEmail = email;
            User user = userRepository.findByEmail(finalEmail)
                    .orElseGet(() -> userRepository.save(User.builder()
                            .email(finalEmail)
                            .username(name)
                            .role(UserRole.USER)
                            .active(true)
                            .provider("FACEBOOK")
                            .build()));
            ensureDefaultUserRole(user);

            String token = jwtTokenProvider.generateTokenFromUsername(user.getUsername());
            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Facebook login failed");
        }
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    private void ensureDefaultUserRole(User user) {
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

