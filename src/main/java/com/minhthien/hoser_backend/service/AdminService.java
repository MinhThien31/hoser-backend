package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.enums.UserRole;

import java.util.List;

public interface AdminService {
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);
    UserResponse updateUserStatus(Long userId, boolean active);
    UserResponse updateUserRole(Long userId, UserRole role);
}
