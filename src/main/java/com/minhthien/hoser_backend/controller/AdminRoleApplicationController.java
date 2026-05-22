package com.minhthien.hoser_backend.controller;

import com.minhthien.hoser_backend.dto.request.AdminReviewRequest;
import com.minhthien.hoser_backend.dto.response.ApiResponse;
import com.minhthien.hoser_backend.dto.response.RoleApplicationResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.service.RoleApplicationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/role-applications")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class AdminRoleApplicationController {
    private final RoleApplicationService roleApplicationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleApplicationResponse>>> getRoleApplications(
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) RoleApprovalStatus status) {
        return ResponseEntity.ok(ApiResponse.success(roleApplicationService.getAdminApplications(role, status)));
    }

    @PutMapping("/{profileId}/approve")
    public ResponseEntity<ApiResponse<RoleApplicationResponse>> approveRoleApplication(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long profileId) {
        return ResponseEntity.ok(ApiResponse.success("Role application approved",
                roleApplicationService.approveApplication(profileId, currentUser.getId())));
    }

    @PutMapping("/{profileId}/reject")
    public ResponseEntity<ApiResponse<RoleApplicationResponse>> rejectRoleApplication(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long profileId,
            @Valid @RequestBody AdminReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Role application rejected",
                roleApplicationService.rejectApplication(profileId, currentUser.getId(), request)));
    }
}
