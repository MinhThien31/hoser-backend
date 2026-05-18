package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(currentUser.getId())));
    }

    @PutMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMe(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deactivated", null));
    }
}
