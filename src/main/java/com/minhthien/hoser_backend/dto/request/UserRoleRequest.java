package com.minhthien.hoser_backend.dto.request;

import com.minhthien.hoser_backend.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRoleRequest {

    @NotNull(message = "Role is required")
    private UserRole role;
}
