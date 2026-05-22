package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.request.UpdatePasswordRequest;
import com.minhthien.hoser_backend.dto.request.UserProfileRequest;
import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.exception.ResourceNotFoundException;
import com.minhthien.hoser_backend.repository.UserRepository;
import com.minhthien.hoser_backend.service.CloudinaryUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryUploadService cloudinaryUploadService;

    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserServiceImpl(userRepository, passwordEncoder, cloudinaryUploadService);
    }

    @Test
    void updatePasswordWithValidCurrentPasswordEncodesAndSavesNewPassword() {
        User user = User.builder()
                .id(1L)
                .password(passwordEncoder.encode("old-password"))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.updatePassword(1L, passwordRequest("old-password", "new-password"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(passwordEncoder.matches("new-password", userCaptor.getValue().getPassword())).isTrue();
        assertThat(passwordEncoder.matches("old-password", userCaptor.getValue().getPassword())).isFalse();
    }

    @Test
    void updateProfileWithAvatarUploadsFileAndStoresReturnedUrl() {
        User user = User.builder()
                .id(1L)
                .username("owner")
                .build();
        UserProfileRequest request = new UserProfileRequest();
        request.setFullName("Updated Owner");
        request.setPhone("0900000000");
        request.setLocation("Ho Chi Minh City");
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.png", "image/png", "img".getBytes());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cloudinaryUploadService.uploadImage(eq(avatar), eq("hoser/users/avatars")))
                .thenReturn("https://cdn.example/users/avatar.png");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile(1L, request, avatar);

        assertThat(response.getFullName()).isEqualTo("Updated Owner");
        assertThat(response.getPhone()).isEqualTo("0900000000");
        assertThat(response.getLocation()).isEqualTo("Ho Chi Minh City");
        assertThat(response.getAvatarUrl()).isEqualTo("https://cdn.example/users/avatar.png");
        verify(cloudinaryUploadService).uploadImage(avatar, "hoser/users/avatars");
    }

    @Test
    void updateProfileKeepsExistingFieldsWhenNotProvided() {
        User user = User.builder()
                .id(1L)
                .username("owner")
                .fullName("Existing Owner")
                .avatarUrl("https://cdn.example/users/existing.png")
                .location("Ho Chi Minh City")
                .build();
        user.setPhone("0900000000");
        UserProfileRequest request = new UserProfileRequest();
        request.setLocation("Da Nang");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateProfile(1L, request);

        assertThat(response.getFullName()).isEqualTo("Existing Owner");
        assertThat(response.getPhone()).isEqualTo("0900000000");
        assertThat(response.getAvatarUrl()).isEqualTo("https://cdn.example/users/existing.png");
        assertThat(response.getLocation()).isEqualTo("Da Nang");
        verify(cloudinaryUploadService, never()).uploadImage(any(), any());
    }

    @Test
    void updatePasswordRejectsIncorrectCurrentPassword() {
        User user = User.builder()
                .id(1L)
                .password(passwordEncoder.encode("old-password"))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword(1L, passwordRequest("wrong-password", "new-password")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Current password is incorrect");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordRejectsNewPasswordMatchingCurrentPassword() {
        User user = User.builder()
                .id(1L)
                .password(passwordEncoder.encode("same-password"))
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword(1L, passwordRequest("same-password", "same-password")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("New password must be different from current password");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordRejectsAccountWithoutLocalPassword() {
        User user = User.builder()
                .id(1L)
                .password(null)
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.updatePassword(1L, passwordRequest("old-password", "new-password")))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Password login is not enabled for this account");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordRejectsMissingUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updatePassword(99L, passwordRequest("old-password", "new-password")))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: '99'");

        verify(userRepository, never()).save(any(User.class));
    }

    private UpdatePasswordRequest passwordRequest(String currentPassword, String newPassword) {
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        request.setCurrentPassword(currentPassword);
        request.setNewPassword(newPassword);
        return request;
    }
}
