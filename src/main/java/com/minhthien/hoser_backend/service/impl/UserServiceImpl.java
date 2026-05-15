package com.minhthien.hoser_backend.service.impl;


import com.minhthien.hoser_backend.dto.request.UpdatePasswordRequest;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return mapToResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean active) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setActive(active);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse selectRole(Long userId, UserRole role) {
        if (role == UserRole.ADMIN) {
            throw new BadRequestException("Admin role can only be assigned by admin");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() != UserRole.USER) {
            throw new BadRequestException("Role already selected");
        }

        user.setRole(role);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, UserRole role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(role);
        return mapToResponse(userRepository.save(user));
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

