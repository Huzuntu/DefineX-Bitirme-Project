package com.definex.task_management.controller;

import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {

    private static final String API_BASE_PATH = "/api/v1/users";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserRequest userRequest;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        userRequest = UserRequest.builder()
                .name("test")
                .email("test@definex.com")
                .password("test123")
                .department("Engineering")
                .role(UserRole.TEAM_MEMBER)
                .build();
        userResponse = UserResponse.builder()
                .id(userId)
                .name("test")
                .email("test@definex.com")
                .department("Engineering")
                .role(UserRole.TEAM_MEMBER)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShouldReturnOk() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()))
                .andExpect(jsonPath("$.department").value(userResponse.getDepartment()))
                .andExpect(jsonPath("$.role").value(userResponse.getRole().toString()));

        verify(userService).createUser(any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void createUser_ShouldReturnForbidden() throws Exception {
        when(userService.createUser(any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void createUser_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = UserRequest.builder()
                .name("")
                .email("invalid-email")
                .password("")
                .department("")
                .role(UserRole.TEAM_MEMBER)
                .build();

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void getAllUsers_ShouldReturnOk() throws Exception {
        List<UserResponse> users = Arrays.asList(userResponse);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get(API_BASE_PATH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(userResponse.getName()))
                .andExpect(jsonPath("$[0].email").value(userResponse.getEmail()));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void getUserById_ShouldReturnOk() throws Exception {
        when(userService.getUserById(userId)).thenReturn(userResponse);

        mockMvc.perform(get(API_BASE_PATH + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void getUserById_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(userId)).thenThrow(new EntityNotFoundException("User not found"));

        mockMvc.perform(get(API_BASE_PATH + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService).getUserById(userId);
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void updateUser_ShouldReturnOk() throws Exception {
        when(userService.updateUser(eq(userId), any(UserRequest.class))).thenReturn(userResponse);

        mockMvc.perform(put(API_BASE_PATH + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(userResponse.getName()))
                .andExpect(jsonPath("$.email").value(userResponse.getEmail()));

        verify(userService).updateUser(eq(userId), any(UserRequest.class));
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void updateUser_ShouldReturnBadRequest() throws Exception {
        UserRequest invalidRequest = UserRequest.builder()
                .name("")
                .email("invalid-email")
                .password("")
                .department("")
                .role(UserRole.TEAM_MEMBER)
                .build();

        mockMvc.perform(put(API_BASE_PATH + "/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnOk() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete(API_BASE_PATH + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_ShouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("User not found")).when(userService).deleteUser(userId);

        mockMvc.perform(delete(API_BASE_PATH + "/{id}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(userId);
    }
}
