package com.minhthien.hoser_backend.config;

import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:admin-user-seed-test;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "app.admin.seed.enabled=true",
        "app.admin.seed.username=seed-admin",
        "app.admin.seed.email=seed-admin@example.com",
        "app.admin.seed.password=SeedPassword123!"
})
class AdminUserInitializerTest {

    @Autowired
    private AdminUserInitializer adminUserInitializer;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void runCreatesAdminUserAndDoesNotDuplicateOnRerun() {
        adminUserInitializer.run(null);
        adminUserInitializer.run(null);

        assertThat(userRepository.findByRole(UserRole.ADMIN)).hasSize(1);
        User admin = userRepository.findByEmail("seed-admin@example.com").orElseThrow();
        assertThat(admin.getUsername()).isEqualTo("seed-admin");
        assertThat(admin.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(admin.getActive()).isTrue();
        assertThat(passwordEncoder.matches("SeedPassword123!", admin.getPassword())).isTrue();
    }
}
