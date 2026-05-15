package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.request.UpdatePasswordRequest;
import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Password updated", null));
    }

    @PutMapping("/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivate(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deactivated", null));
    }

    @PutMapping("/activate/{userId}")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@PathVariable Long userId) {
        userService.activateAccount(userId);
        return ResponseEntity.ok(ApiResponse.success("Account activated", null));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean active) {

        UserResponse response = userService.updateUserStatus(userId, active);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

