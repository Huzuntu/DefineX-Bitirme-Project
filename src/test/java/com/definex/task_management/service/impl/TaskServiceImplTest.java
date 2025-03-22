package com.definex.task_management.service.impl;

import com.definex.task_management.dto.TaskRequest;
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
import com.definex.task_management.exception.InvalidStateTransitionException;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.ProjectTaskService;
import com.definex.task_management.service.UserService;
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
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectTaskService projectTaskService;

    @Mock
    private UserService userService;


    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskServiceImpl taskService;

    private UUID taskId;
    private UUID projectId;
    private UUID userId;
    private Task task;
    private Project project;
    private User user;
    private TaskRequest taskRequest;
    private CustomUserDetails customUserDetails;
    private Set<UUID> assigneeIds;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        projectId = UUID.randomUUID();
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
                .assignees(new HashSet<>(Collections.singletonList(user)))
                .build();

        assigneeIds = new HashSet<>(Collections.singletonList(userId));

        taskRequest = TaskRequest.builder()
                .title("Test Task")
                .userStory("Test User Story")
                .acceptanceCriteria("Test Acceptance Criteria")
                .priority(TaskPriority.MEDIUM)
                .projectId(projectId)
                .assigneeIds(assigneeIds)
                .build();

        customUserDetails = new CustomUserDetails(user);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getPrincipal()).thenReturn(customUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createTask_Success() {
        when(projectTaskService.getProjectEntityByIdWithValidation(projectId, userId))
                .thenReturn(project);
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.createTask(taskRequest);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(taskRequest.getTitle(), response.getTitle());
        assertEquals(taskRequest.getUserStory(), response.getUserStory());
        assertEquals(TaskState.BACKLOG, response.getState());
        assertEquals(taskRequest.getPriority(), response.getPriority());

        verify(projectTaskService).getProjectEntityByIdWithValidation(projectId, userId);
        verify(userService).getUserEntityById(userId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(projectTaskService.getProjectEntityByIdWithValidation(any(), any()))
                .thenThrow(new DeniedAccessException("Access denied"));

        assertThrows(DeniedAccessException.class,
                () -> taskService.createTask(taskRequest));

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.getTaskById(taskId);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(task.getTitle(), response.getTitle());
        assertEquals(task.getUserStory(), response.getUserStory());
        assertEquals(task.getState(), response.getState());
        assertEquals(task.getPriority(), response.getPriority());

        verify(taskRepository).findById(taskId);
    }

    @Test
    void getTaskById_NotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> taskService.getTaskById(taskId));

        verify(taskRepository).findById(taskId);
    }

    @Test
    void updateTaskState_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTaskState(taskId, TaskState.IN_ANALYSIS, "Reason");

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(TaskState.IN_ANALYSIS, response.getState());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTaskState_InvalidTransition() {
        task.setState(TaskState.COMPLETED);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(InvalidStateTransitionException.class,
                () -> taskService.updateTaskState(taskId, TaskState.BACKLOG, "Reason"));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskPriority_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTaskPriority(taskId, TaskPriority.HIGH);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(TaskPriority.HIGH, response.getPriority());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void assignTask_Success() {
        UUID newUserId = UUID.randomUUID();
        User newUser = User.builder()
                .id(newUserId)
                .department("IT")
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userService.getUserEntityById(newUserId)).thenReturn(newUser);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.assignUserToTask(taskId, newUserId);

        assertNotNull(response);
        assertEquals(taskId, response.getId());

        verify(taskRepository).findById(taskId);
        verify(userService).getUserEntityById(newUserId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void unassignTask_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.removeUserFromTask(taskId, userId);

        assertNotNull(response);
        assertEquals(taskId, response.getId());

        verify(taskRepository).findById(taskId);
        verify(userService).getUserEntityById(userId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void deleteTask_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        taskService.deleteTask(taskId);

        verify(taskRepository).findById(taskId);
        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_NotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> taskService.deleteTask(taskId));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void updateTask_Success() {
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .userStory("Updated User Story")
                .acceptanceCriteria("Updated Acceptance Criteria")
                .priority(TaskPriority.HIGH)
                .projectId(projectId)
                .assigneeIds(new HashSet<>(Collections.singletonList(userId)))
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.updateTask(taskId, updateRequest);

        assertNotNull(response);
        assertEquals(taskId, response.getId());
        assertEquals(updateRequest.getTitle(), response.getTitle());
        assertEquals(updateRequest.getUserStory(), response.getUserStory());
        assertEquals(updateRequest.getAcceptanceCriteria(), response.getAcceptanceCriteria());
        assertEquals(updateRequest.getPriority(), response.getPriority());

        verify(taskRepository).findById(taskId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void updateTask_NotFound() {
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .userStory("Updated User Story")
                .acceptanceCriteria("Updated Acceptance Criteria")
                .priority(TaskPriority.HIGH)
                .projectId(projectId)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> taskService.updateTask(taskId, updateRequest));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_DifferentProject() {
        UUID differentProjectId = UUID.randomUUID();
        TaskRequest updateRequest = TaskRequest.builder()
                .title("Updated Task")
                .userStory("Updated User Story")
                .acceptanceCriteria("Updated Acceptance Criteria")
                .priority(TaskPriority.HIGH)
                .projectId(differentProjectId)
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(DeniedAccessException.class,
                () -> taskService.updateTask(taskId, updateRequest));

        verify(taskRepository).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getAllTasksUnderProject_Success() {
        List<Task> tasks = Collections.singletonList(task);
        when(projectTaskService.getProjectEntityByIdWithValidation(projectId, userId))
                .thenReturn(project);
        when(taskRepository.findByProjectId(projectId)).thenReturn(tasks);

        List<TaskResponse> responses = taskService.getAllTasksUnderProject(projectId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(taskId, responses.get(0).getId());
        assertEquals(task.getTitle(), responses.get(0).getTitle());
        assertEquals(projectId, responses.get(0).getProjectId());

        verify(projectTaskService).getProjectEntityByIdWithValidation(projectId, userId);
        verify(taskRepository).findByProjectId(projectId);
    }

    @Test
    void getAllTasksUnderProject_UnauthorizedAccess() {
        when(projectTaskService.getProjectEntityByIdWithValidation(projectId, userId))
                .thenThrow(new DeniedAccessException("Access denied"));

        assertThrows(DeniedAccessException.class,
                () -> taskService.getAllTasksUnderProject(projectId));

        verify(projectTaskService).getProjectEntityByIdWithValidation(projectId, userId);
        verify(taskRepository, never()).findByProjectId(any());
    }

}