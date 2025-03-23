package com.definex.task_management.controller;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.ProjectStatus;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.UserMapper;
import com.definex.task_management.service.ProjectService;
import com.definex.task_management.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;


import java.util.Arrays;
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
public class ProjectControllerTest {
    private static final String API_BASE_PATH = "/api/v1/projects";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private TaskService taskService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ProjectRequest projectRequest;
    private ProjectResponse projectResponse;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        UUID user1Id = UUID.randomUUID();
        UUID user2Id = UUID.randomUUID();

        User user1 = User.builder()
                .id(user1Id)
                .name("test1")
                .email("test1@definex.com")
                .password("test1")
                .role(UserRole.PROJECT_GROUP_MANAGER)
                .department("Engineering")
                .build();
        User user2 = User.builder()
                .id(user2Id)
                .name("test2")
                .email("test2@definex.com")
                .password("test2")
                .role(UserRole.TEAM_MEMBER)
                .department("Engineering")
                .build();

        projectId = UUID.randomUUID();
        projectRequest = ProjectRequest.builder()
                .title("Test title")
                .department("Engineering")
                .description("Test description")
                .teamMemberIds(Set.of(user1Id, user2Id))
                .build();

        projectResponse = ProjectResponse.builder()
                .id(projectId)
                .title("Test title")
                .description("Test description")
                .department("Engineering")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(Set.of(UserMapper.toResponse(user1), UserMapper.toResponse(user2)))
                .build();
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void createProject_ShouldReturnOk() throws Exception {
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value(projectRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(projectRequest.getDescription()))
                .andExpect(jsonPath("$.department").value(projectRequest.getDepartment()));
        verify(projectService, times(1)).createProject(any(ProjectRequest.class));
    }

    @Test
    @WithMockUser(roles = "TEAM_MEMBER")
    void createProject_ShouldReturnForbidden() throws Exception {
        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void createProject_ShouldReturnBadRequest() throws Exception {
        ProjectRequest invalidRequest = ProjectRequest.builder()
                .title("")
                .department("")
                .description("")
                .teamMemberIds(Set.of())
                .build();

        mockMvc.perform(post(API_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "PROJECT_GROUP_MANAGER")
    void getProjectsByDepartment_ShouldReturnOk() throws Exception {
        List<ProjectResponse> projects = Arrays.asList(projectResponse);
        when(projectService.getProjectsByDepartment(any())).thenReturn(projects);

        mockMvc.perform(get(API_BASE_PATH + "/department/" + "{department}", projectRequest.getDepartment())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getProjectsByDepartment_ShouldReturnOk_WhenAccessingSameDepartment() throws Exception {
        String userDepartment = "Engineering";
        List<ProjectResponse> projects = Arrays.asList(projectResponse);
        when(projectService.getProjectsByDepartment(userDepartment)).thenReturn(projects);

        mockMvc.perform(get(API_BASE_PATH + "/department/{department}", userDepartment))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value(userDepartment))
                .andExpect(jsonPath("$[0].title").value(projectResponse.getTitle()))
                .andExpect(jsonPath("$[0].description").value(projectResponse.getDescription()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getProjectsByDepartment_ShouldReturnForbidden_WhenAccessingDifferentDepartment() throws Exception {
        String targetDepartment = "IT";
        when(projectService.getProjectsByDepartment(targetDepartment))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(get(API_BASE_PATH + "/department/{department}", targetDepartment))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getProjectById_ShouldReturnOk() throws Exception {
        when(projectService.getProjectById(projectId)).thenReturn(projectResponse);

        mockMvc.perform(get(API_BASE_PATH + "/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value(projectResponse.getTitle()))
                .andExpect(jsonPath("$.department").value(projectResponse.getDepartment()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getProjectById_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(projectService.getProjectById(nonExistentId))
                .thenThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId));

        mockMvc.perform(get(API_BASE_PATH + "/{projectId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void getProjectById_ShouldReturnForbidden_WhenAccessingDifferentProject() throws Exception {
        when(projectService.getProjectById(projectId))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(get(API_BASE_PATH + "/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateProject_ShouldReturnOk() throws Exception {
        when(projectService.updateProject(eq(projectId), any(ProjectRequest.class))).thenReturn(projectResponse);

        mockMvc.perform(put(API_BASE_PATH + "/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value(projectRequest.getTitle()))
                .andExpect(jsonPath("$.department").value(projectRequest.getDepartment()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateProject_ShouldReturnBadRequest() throws Exception {
        ProjectRequest invalidRequest = ProjectRequest.builder().build();

        mockMvc.perform(put(API_BASE_PATH + "/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void updateProject_ShouldReturnForbidden_WhenAccessingDifferentProject() throws Exception {
        when(projectService.updateProject(eq(projectId), any(ProjectRequest.class)))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(put(API_BASE_PATH + "/{projectId}", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateProject_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(projectService.updateProject(eq(nonExistentId), any(ProjectRequest.class)))
                .thenThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId));

        mockMvc.perform(put(API_BASE_PATH + "/{projectId}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectRequest)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateProjectStatus_ShouldReturnOk() throws Exception {
        String status = ProjectStatus.COMPLETED.toString();
        when(projectService.updateProjectStatus(projectId, status)).thenReturn(projectResponse);

        mockMvc.perform(patch(API_BASE_PATH + "/{projectId}/status/{status}", projectId, status))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void updateProjectStatus_ShouldReturnForbidden_WhenAccessingDifferentProject() throws Exception {
        String status = ProjectStatus.COMPLETED.toString();
        when(projectService.updateProjectStatus(projectId, status))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(patch(API_BASE_PATH + "/{projectId}/status/{status}", projectId, status))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void updateProjectStatus_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        String status = ProjectStatus.COMPLETED.toString();
        when(projectService.updateProjectStatus(nonExistentId, status))
                .thenThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId));

        mockMvc.perform(patch(API_BASE_PATH + "/{projectId}/status/{status}", nonExistentId, status))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void assignTask_ShouldReturnOk() throws Exception {
        UUID taskId = UUID.randomUUID();
        TaskResponse taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Criteria")
                .state(TaskState.BACKLOG)
                .priority(TaskPriority.HIGH)
                .build();

        when(taskService.getTaskById(taskId)).thenReturn(taskResponse);
        when(projectService.assignTask(projectId, taskId)).thenReturn(projectResponse);

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/tasks/{taskId}", projectId, taskId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.title").value(projectResponse.getTitle()))
                .andExpect(jsonPath("$.department").value(projectResponse.getDepartment()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void assignTask_ShouldReturnForbidden_WhenAccessingDifferentTask() throws Exception {
        UUID taskId = UUID.randomUUID();
        when(projectService.assignTask(projectId, taskId))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/tasks/{taskId}", projectId, taskId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void assignTask_ShouldReturnNotFound() throws Exception {
        UUID nonExistentTaskId = UUID.randomUUID();
        String errorMessage = "Task not found with id: " + nonExistentTaskId;
        when(taskService.getTaskById(nonExistentTaskId))
                .thenThrow(new EntityNotFoundException(errorMessage));
        when(projectService.assignTask(projectId, nonExistentTaskId))
                .thenThrow(new EntityNotFoundException(errorMessage));

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/tasks/{taskId}", projectId, nonExistentTaskId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void addTeamMember_ShouldReturnOk() throws Exception {
        UUID newMemberId = UUID.randomUUID();
        when(projectService.addTeamMember(projectId, newMemberId)).thenReturn(projectResponse);

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, newMemberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void addTeamMember_ShouldReturnNotFound_WhenAccessingProject() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UUID newMemberId = UUID.randomUUID();
        when(projectService.addTeamMember(nonExistentId, newMemberId))
                .thenThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId));

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/members/{userId}", nonExistentId, newMemberId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void addTeamMember_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        when(projectService.addTeamMember(projectId, nonExistentUserId))
                .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentUserId));

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, nonExistentUserId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void addTeamMember_ShouldReturnForbidden_WhenAccessingProject() throws Exception {
        UUID newMemberId = UUID.randomUUID();
        when(projectService.addTeamMember(projectId, newMemberId))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(post(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, newMemberId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void removeTeamMember_ShouldReturnOk() throws Exception {
        UUID memberId = UUID.randomUUID();
        when(projectService.removeTeamMember(projectId, memberId)).thenReturn(projectResponse);

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, memberId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void removeTeamMember_ShouldReturnNotFound_WhenAccessingProject() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        when(projectService.removeTeamMember(nonExistentId, memberId))
                .thenThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId));

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}/members/{userId}", nonExistentId, memberId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void removeTeamMember_ShouldReturnNotFound_WhenUserDoesNotExist() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        when(projectService.removeTeamMember(projectId, nonExistentUserId))
                .thenThrow(new EntityNotFoundException("User not found with id: " + nonExistentUserId));

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, nonExistentUserId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void removeTeamMember_ShouldReturnForbidden_WhenAccessingProject() throws Exception {
        UUID memberId = UUID.randomUUID();
        when(projectService.removeTeamMember(projectId, memberId))
                .thenThrow(new DeniedAccessException("User does not have access to resources in another department"));

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}/members/{userId}", projectId, memberId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void deleteProject_ShouldReturnOk() throws Exception {
        doNothing().when(projectService).deleteProject(projectId);

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void deleteProject_ShouldReturnNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Project not found with id: " + nonExistentId))
                .when(projectService).deleteProject(nonExistentId);

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}", nonExistentId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "IT"})
    void deleteProject_ShouldReturnForbidden_WhenAccessingProject() throws Exception {
        doThrow(new DeniedAccessException("User does not have access to resources in another department"))
                .when(projectService).deleteProject(projectId);

        mockMvc.perform(delete(API_BASE_PATH + "/{projectId}", projectId))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("User does not have access to resources in another department"));
    }
}
