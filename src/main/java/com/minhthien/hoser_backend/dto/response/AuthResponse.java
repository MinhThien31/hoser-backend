package com.minhthien.hoser_backend.dto.response;

import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.enums.RoleApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String phone;
    private String email;
    private UserRole role;
    private UserRole pendingRole;
    private RoleApprovalStatus roleApprovalStatus;
    private String roleReviewReason;
    private String fullName;
}
