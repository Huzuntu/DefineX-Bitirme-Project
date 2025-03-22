package com.definex.task_management.service.impl;

import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.ProjectStatus;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.repository.ProjectRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.BaseService;
import com.definex.task_management.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectTaskServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private BaseService baseService;

    @InjectMocks
    private ProjectTaskServiceImpl projectTaskService;

    private UUID projectId;
    private UUID taskId;
    private UUID userId;
    private Project project;
    private Task task;
    private User user;
    private TaskResponse taskResponse;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.PROJECT_MANAGER)
                .department("IT")
                .build();

        project = Project.builder()
                .id(projectId)
                .title("Test Project")
                .description("Test Description")
                .department("IT")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(new HashSet<>(Collections.singletonList(user)))
                .tasks(new HashSet<>())
                .build();

        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Acceptance Criteria")
                .state(TaskState.BACKLOG)
                .priority(TaskPriority.MEDIUM)
                .project(project)
                .build();

        taskResponse = TaskResponse.builder()
                .id(taskId)
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Acceptance Criteria")
                .state(TaskState.BACKLOG)
                .priority(TaskPriority.MEDIUM)
                .projectId(projectId)
                .build();

        customUserDetails = new CustomUserDetails(user);

    }

    private void setupSecurityContext(CustomUserDetails userDetails) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void assignTaskToProject_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskService.getTaskById(taskId)).thenReturn(taskResponse);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectTaskService.assignTaskToProject(projectId, taskId, customUserDetails);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(project.getTitle(), response.getTitle());

        verify(projectRepository).findById(projectId);
        verify(taskService).getTaskById(taskId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void assignTaskToProject_ProjectNotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectTaskService.assignTaskToProject(projectId, taskId, customUserDetails));

        verify(projectRepository).findById(projectId);
        verify(taskService, never()).getTaskById(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void assignTaskToProject_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(DeniedAccessException.class,
                () -> projectTaskService.assignTaskToProject(projectId, taskId, differentUserDetails));

        verify(projectRepository).findById(projectId);
        verify(taskService, never()).getTaskById(any());
        verify(projectRepository, never()).save(any());
    }

    @Test
    void getProjectEntityById_Success() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Project result = projectTaskService.getProjectEntityById(projectId);

        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals(project.getTitle(), result.getTitle());

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectEntityById_NotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectTaskService.getProjectEntityById(projectId));

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectEntityByIdWithValidation_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        Project result = projectTaskService.getProjectEntityByIdWithValidation(projectId, userId);

        assertNotNull(result);
        assertEquals(projectId, result.getId());
        assertEquals(project.getTitle(), result.getTitle());

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectEntityByIdWithValidation_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("IT")
                .build();

        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        setupSecurityContext(differentUserDetails);

        Project projectWithoutUser = Project.builder()
                .id(projectId)
                .title("Test Project")
                .description("Test Description")
                .department("IT")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(new HashSet<>())
                .tasks(new HashSet<>())
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectWithoutUser));

        assertThrows(DeniedAccessException.class,
                () -> projectTaskService.getProjectEntityByIdWithValidation(projectId, differentUser.getId()));

        verify(projectRepository).findById(projectId);
    }
}