package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.request.UserProfileRequest;
import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.exception.UnauthorizedException;
import com.minhthien.hoser_backend.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/me/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthenticationPrincipal User currentUser) {
        requireAuthenticated(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userService.getCurrentUser(currentUser.getId())));
    }

    @PutMapping(value = "/me/profile", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody UserProfileRequest request) {
        requireAuthenticated(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(currentUser.getId(), request)));
    }

    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfileWithAvatar(
            @AuthenticationPrincipal User currentUser,
            @Valid @ModelAttribute UserProfileRequest request) {
        requireAuthenticated(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile updated",
                userService.updateProfile(currentUser.getId(), request, request.getAvatar())));
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getPublicProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getPublicProfile(id)));
    }

    @PutMapping("/me/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMe(@AuthenticationPrincipal User currentUser) {
        userService.deactivateAccount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Account deactivated", null));
    }

    private void requireAuthenticated(User currentUser) {
        if (currentUser == null) {
            throw new UnauthorizedException("Authentication is required");
        }
    }
}
