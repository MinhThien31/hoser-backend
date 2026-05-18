package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.UpdatePasswordRequest;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.enums.UserRole;

public interface UserService {
    UserResponse getCurrentUser(Long userId);

    void updatePassword(Long userId, UpdatePasswordRequest request);

    void deactivateAccount(Long userId);

    UserResponse selectRole(Long userId, UserRole role);
}
