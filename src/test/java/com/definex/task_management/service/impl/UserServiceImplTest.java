package com.definex.task_management.service.impl;

import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRequest userRequest;
    private User user;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userRequest = UserRequest.builder()
                .name("Test User")
                .email("test@example.com")
                .password("password123")
                .role(UserRole.TEAM_MEMBER)
                .department("IT")
                .build();

        user = User.builder()
                .id(userId)
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .password("encodedPassword")
                .role(userRequest.getRole())
                .department(userRequest.getDepartment())
                .build();
    }

    @Test
    void createUser_Success() {
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.createUser(userRequest);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(userRequest.getName(), response.getName());
        assertEquals(userRequest.getEmail(), response.getEmail());
        assertEquals(userRequest.getRole(), response.getRole());
        assertEquals(userRequest.getDepartment(), response.getDepartment());

        verify(passwordEncoder).encode(userRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UserResponse response = userService.getUserById(userId);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRole(), response.getRole());
        assertEquals(user.getDepartment(), response.getDepartment());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_ThrowsEntityNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityById_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUserEntityById(userId);
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getRole(), result.getRole());
        assertEquals(user.getDepartment(), result.getDepartment());

        verify(userRepository).findById(userId);
    }

    @Test
    void getUserEntityById_ThrowsEntityNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserEntityById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void getAllUsers_Success() {
        User user2 = User.builder()
                .id(UUID.randomUUID())
                .name("Test User 2")
                .email("test2@example.com")
                .password("encodedPassword2")
                .role(UserRole.TEAM_LEADER)
                .department("HR")
                .build();

        List<User> users = Arrays.asList(user, user2);
        when(userRepository.findAll()).thenReturn(users);

        List<UserResponse> responses = userService.getAllUsers();

        assertNotNull(responses);
        assertEquals(2, responses.size());
        
        UserResponse firstResponse = responses.get(0);
        assertEquals(user.getId(), firstResponse.getId());
        assertEquals(user.getName(), firstResponse.getName());
        assertEquals(user.getEmail(), firstResponse.getEmail());
        assertEquals(user.getRole(), firstResponse.getRole());
        assertEquals(user.getDepartment(), firstResponse.getDepartment());

        UserResponse secondResponse = responses.get(1);
        assertEquals(user2.getId(), secondResponse.getId());
        assertEquals(user2.getName(), secondResponse.getName());
        assertEquals(user2.getEmail(), secondResponse.getEmail());
        assertEquals(user2.getRole(), secondResponse.getRole());
        assertEquals(user2.getDepartment(), secondResponse.getDepartment());

        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        UserRequest updateRequest = UserRequest.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .password("newPassword123")
                .role(UserRole.TEAM_LEADER)
                .department("HR")
                .build();

        User existingUser = User.builder()
                .id(userId)
                .name("Old Name")
                .email("old@example.com")
                .password("oldEncodedPassword")
                .role(UserRole.TEAM_MEMBER)
                .department("IT")
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .name(updateRequest.getName())
                .email(updateRequest.getEmail())
                .password("newEncodedPassword")
                .role(updateRequest.getRole())
                .department(updateRequest.getDepartment())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(updateRequest.getPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserResponse response = userService.updateUser(userId, updateRequest);

        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals(updateRequest.getName(), response.getName());
        assertEquals(updateRequest.getEmail(), response.getEmail());
        assertEquals(updateRequest.getRole(), response.getRole());
        assertEquals(updateRequest.getDepartment(), response.getDepartment());

        verify(userRepository).findById(userId);
        verify(passwordEncoder).encode(updateRequest.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_ThrowsEntityNotFoundException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(userId, userRequest));
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).existsById(userId);
        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_ThrowsEntityNotFoundException() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));
        verify(userRepository).existsById(userId);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void hasAnyUsers_ReturnsTrue() {
        when(userRepository.count()).thenReturn(1L);

        boolean result = userService.hasAnyUsers();

        assertTrue(result);
        verify(userRepository).count();
    }

    @Test
    void hasAnyUsers_ReturnsFalse() {
        boolean result = userService.hasAnyUsers();
        assertFalse(result);
        verify(userRepository).count();
    }
} 