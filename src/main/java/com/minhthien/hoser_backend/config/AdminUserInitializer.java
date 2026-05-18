package com.minhthien.hoser_backend.config;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@Order(1)
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed.enabled:true}")
    private boolean enabled;

    @Value("${app.admin.seed.username:admin}")
    private String username;

    @Value("${app.admin.seed.email:admin@example.local}")
    private String email;

    @Value("${app.admin.seed.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        validateSeedConfig();

        Optional<User> existingUser = userRepository.findByEmail(email)
                .or(() -> userRepository.findByUsername(username));

        User admin = existingUser.orElseGet(User::new);
        if (admin.getId() == null) {
            admin.setUsername(username);
            admin.setEmail(email);
            admin.setPassword(passwordEncoder.encode(password));
        } else if (admin.getPassword() == null || admin.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(password));
        }

        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin.setCreatedBy("SYSTEM");
        admin.setUpdatedBy("SYSTEM");
        userRepository.save(admin);
    }

    private void validateSeedConfig() {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("app.admin.seed.username must not be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalStateException("app.admin.seed.email must not be blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("app.admin.seed.password must not be blank when admin seed is enabled");
        }
    }
}
