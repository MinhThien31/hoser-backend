package com.minhthien.hoser_backend.service.impl;

import com.minhthien.hoser_backend.dto.response.UserResponse;
import com.minhthien.hoser_backend.entity.User;
import com.minhthien.hoser_backend.enums.UserRole;
import com.minhthien.hoser_backend.exception.BadRequestException;
import com.minhthien.hoser_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void selectRoleAllowsUserToPickNonAdminRoleOnce() {
        User user = User.builder()
                .id(1L)
                .username("owner")
                .email("owner@example.com")
                .role(UserRole.USER)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.selectRole(1L, UserRole.OWNER);

        assertEquals(UserRole.OWNER, response.getRole());
    }

    @Test
    void selectRoleRejectsAdminRole() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.selectRole(1L, UserRole.ADMIN)
        );

        assertEquals("Admin role can only be assigned by admin", exception.getMessage());
    }

    @Test
    void selectRoleRejectsUserThatAlreadySelectedRole() {
        User user = User.builder()
                .id(1L)
                .username("jockey")
                .email("jockey@example.com")
                .role(UserRole.JOCKEY)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> userService.selectRole(1L, UserRole.OWNER)
        );

        assertEquals("Role already selected", exception.getMessage());
    }

    @Test
    void updateUserRoleAllowsAdminToAssignAnyRole() {
        User user = User.builder()
                .id(1L)
                .username("candidate")
                .email("candidate@example.com")
                .role(UserRole.USER)
                .active(true)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse response = userService.updateUserRole(1L, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, response.getRole());
    }
}
