package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.request.AdminReviewRequest;
import com.minhthien.hoser_backend.dto.request.JockeyProfileRequest;
import com.minhthien.hoser_backend.dto.request.JockeyProfileUpdateRequest;
import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.JockeyProfileResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.JockeyStatus;
import com.minhthien.hoser_backend.service.JockeyProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class JockeyProfileController {
    private final JockeyProfileService jockeyProfileService;

    @GetMapping("/jockey/profile")
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.success(jockeyProfileService.getMyProfile(currentUser.getId())));
    }

    @PostMapping(value = "/jockey/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> createMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @ModelAttribute JockeyProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Jockey profile created",
                jockeyProfileService.createMyProfile(
                        currentUser.getId(), request, request.getAvatar(), request.getLicenseDocument())));
    }

    @PutMapping(value = "/jockey/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> updateMyProfile(
            @AuthenticationPrincipal User currentUser,
            @Valid @ModelAttribute JockeyProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Jockey profile saved",
                jockeyProfileService.updateMyProfile(
                        currentUser.getId(), request, request.getAvatar(), request.getLicenseDocument())));
    }

    @GetMapping("/jockeys/available")
    public ResponseEntity<ApiResponse<List<JockeyProfileResponse>>> getAvailableJockeys() {
        return ResponseEntity.ok(ApiResponse.success(jockeyProfileService.getAvailableJockeys()));
    }

    @GetMapping("/jockeys/{id}")
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> getApprovedJockeyProfile(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(jockeyProfileService.getApprovedJockeyProfile(id)));
    }

    @GetMapping("/admin/jockey-profiles")
    public ResponseEntity<ApiResponse<List<JockeyProfileResponse>>> getAdminJockeyProfiles(
            @RequestParam(required = false, defaultValue = "PENDING") JockeyStatus status) {
        return ResponseEntity.ok(ApiResponse.success(jockeyProfileService.getAdminJockeyProfiles(status)));
    }

    @PutMapping("/admin/jockey-profiles/{id}/approve")
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> approveJockeyProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Jockey profile approved",
                jockeyProfileService.approveJockeyProfile(id, currentUser.getId())));
    }

    @PutMapping("/admin/jockey-profiles/{id}/reject")
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> rejectJockeyProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Jockey profile rejected",
                jockeyProfileService.rejectJockeyProfile(id, currentUser.getId(), request)));
    }

    @PutMapping("/admin/jockey-profiles/{id}/suspend")
    public ResponseEntity<ApiResponse<JockeyProfileResponse>> suspendJockeyProfile(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AdminReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Jockey profile suspended",
                jockeyProfileService.suspendJockeyProfile(id, currentUser.getId(), request)));
    }
}
