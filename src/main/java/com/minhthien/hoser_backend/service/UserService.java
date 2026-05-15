package com.minhthien.hoser_backend.service;

import com.minhthien.hoser_backend.dto.request.UpdatePasswordRequest;
import com.minhthien.hoser_backend.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);
    UserResponse updateUserStatus(Long userId, boolean active);
}
