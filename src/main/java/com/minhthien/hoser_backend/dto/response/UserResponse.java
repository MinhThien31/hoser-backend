package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private UserRole role;
    private UserRole pendingRole;
    private RoleApprovalStatus roleApprovalStatus;
    private String roleReviewReason;
    private Long roleReviewedBy;
    private LocalDateTime roleReviewedAt;
    private Boolean active;
    private String avatarUrl;
    private String location;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

