package com.definex.task_management.controller;

import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.dto.TaskRequest;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.dto.TaskStateUpdateRequest;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.ProjectStatus;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.UserMapper;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
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
public class TaskControllerTest {
    private static final String API_BASE_PATH = "/api/v1/tasks";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private TaskRequest taskRequest;
    private TaskResponse taskResponse;
    private UUID taskId;
    private UUID projectId;
    private UUID userId;
    private ProjectResponse projectResponse;
    private User user2;
    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .name("test")
                .email("test@definex.com")
                .password("test")
                .role(UserRole.TEAM_MEMBER)
                .department("Engineering")
                .build();

        user2 = User.builder()
                .id(UUID.randomUUID())
                .name("test")
                .email("test@definex.com")
                .password("test")
                .role(UserRole.TEAM_MEMBER)
                .department("IT")
                .build();

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Criteria")
                .priority(TaskPriority.HIGH)
                .projectId(projectId)
                .assigneeIds(Set.of(userId))
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Criteria")
                .state(TaskState.BACKLOG)
                .priority(TaskPriority.HIGH)
                .projectId(projectId)
                .projectTitle("Test Project")
                .assignees(Set.of(UserMapper.toResponse(user)))
                .build();

        projectResponse = ProjectResponse.builder()
                .id(projectId)
                .title("Test Project")
                .department("Engineering")
                .description("Test Description")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(Set.of(UserMapper.toResponse(user)))
                .tasks(List.of(taskResponse))
                .build();

    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void createTask_ShouldReturnOk() throws Exception {
        when(taskService.createTask(any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(taskRequest.getTitle()))
                .andExpect(jsonPath("$.userStory").value(taskRequest.getUserStory()))
                .andExpect(jsonPath("$.priority").value(taskRequest.getPriority().toString()));
        verify(taskService, times(1)).createTask(any(TaskRequest.class));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "Engineering"})
    void createTask_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void createTask_ShouldReturnBadRequest() throws Exception {
        TaskRequest invalidRequest = TaskRequest.builder()
                .title("")
                .userStory("")
                .acceptanceCriteria("")
                .build();

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getTaskById_ShouldReturnOk() throws Exception {
        when(taskService.getTaskById(taskId)).thenReturn(taskResponse);

        mockMvc.perform(get(API_BASE_PATH + "/{taskId}", taskId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(taskResponse.getTitle()))
                .andExpect(jsonPath("$.userStory").value(taskResponse.getUserStory()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getTaskById_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(taskService.getTaskById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + nonExistentId));

        mockMvc.perform(get(API_BASE_PATH + "/{taskId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + nonExistentId));
    }

    @Test
    void getTaskById_ShouldReturnForbidden_WhenAccessingDifferentDepartment() throws Exception {
        CustomUserDetails principal = new CustomUserDetails(user2);
        principal.setDepartment("IT");

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, "password", principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(taskService.getTaskById(taskId))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(get(API_BASE_PATH + "/{taskId}", taskId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAllTasksUnderProject_ShouldReturnOk() throws Exception {
        List<TaskResponse> tasks = List.of(taskResponse);
        when(taskService.getAllTasksUnderProject(projectId)).thenReturn(tasks);

        mockMvc.perform(get(API_BASE_PATH + "/project/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(taskId.toString()))
                .andExpect(jsonPath("$[0].projectId").value(projectId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateTask_ShouldReturnOk() throws Exception {
        when(taskService.updateTask(eq(taskId), any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(put(API_BASE_PATH + "/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value(taskRequest.getTitle()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "Engineering"})
    void updateTask_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(put(API_BASE_PATH + "/{taskId}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateTask_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(taskService.updateTask(eq(nonExistentId), any(TaskRequest.class)))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + nonExistentId));

        mockMvc.perform(put(API_BASE_PATH + "/{taskId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + nonExistentId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void deleteTask_ShouldReturnOk() throws Exception {
        when(taskService.deleteTask(taskId)).thenReturn(taskResponse);

        mockMvc.perform(delete(API_BASE_PATH + "/{taskId}", taskId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "Engineering"})
    void deleteTask_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/{taskId}", taskId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateTaskState_ShouldReturnOk() throws Exception {
        TaskStateUpdateRequest stateRequest = TaskStateUpdateRequest.builder()
                .newState(TaskState.IN_DEVELOPMENT)
                .reason("Started working")
                .build();
        when(taskService.updateTaskState(taskId, stateRequest.getNewState(), stateRequest.getReason()))
                .thenReturn(taskResponse);

        mockMvc.perform(patch(API_BASE_PATH + "/{taskId}/state", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stateRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateTaskPriority_ShouldReturnOk() throws Exception {
        when(taskService.updateTaskPriority(taskId, TaskPriority.HIGH)).thenReturn(taskResponse);

        mockMvc.perform(patch(API_BASE_PATH + "/{taskId}/priority", taskId)
                        .param("priority", TaskPriority.HIGH.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void assignUserToTask_ShouldReturnOk() throws Exception {
        when(taskService.assignUserToTask(taskId, userId)).thenReturn(taskResponse);

        mockMvc.perform(post(API_BASE_PATH + "/{taskId}/assignees/{userId}", taskId, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void removeUserFromTask_ShouldReturnOk() throws Exception {
        when(taskService.removeUserFromTask(taskId, userId)).thenReturn(taskResponse);

        mockMvc.perform(delete(API_BASE_PATH + "/{taskId}/assignees/{userId}", taskId, userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void removeUserFromTask_ShouldReturnNotFound_WhenUserNotAssigned() throws Exception {
        when(taskService.removeUserFromTask(taskId, userId))
                .thenThrow(new EntityNotFoundException("User is not assigned to this task"));

        mockMvc.perform(delete(API_BASE_PATH + "/{taskId}/assignees/{userId}", taskId, userId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User is not assigned to this task"));
    }
}
