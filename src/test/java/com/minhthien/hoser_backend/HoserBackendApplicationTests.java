package com.minhthien.hoser_backend;

import com.minhthien.hoser_backend.dto.request.LoginRequest;
import com.minhthien.hoser_backend.dto.request.RegisterRequest;
import com.minhthien.hoser_backend.dto.response.AuthResponse;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.service.AuthService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HoserBackendApplicationTests {

    @Autowired
    private AuthService authService;

    @Test
    void contextLoads() {
    }

    @Test
    void registerAndLoginDefaultToUserRole() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("defaultuser");
        registerRequest.setFullName("Default User");
        registerRequest.setEmail("defaultuser@example.com");
        registerRequest.setPassword("password123");

        AuthResponse registerResponse = authService.register(registerRequest);

        Assertions.assertEquals(UserRole.USER, registerResponse.getRole());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("defaultuser@example.com");
        loginRequest.setPassword("password123");

        AuthResponse loginResponse = authService.login(loginRequest);

        Assertions.assertEquals(UserRole.USER, loginResponse.getRole());
    }

}
