package com.definex.task_management.service.impl;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.ProjectStatus;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.exception.InvalidStateTransitionException;
import com.definex.task_management.repository.ProjectRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.TaskService;
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
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private UUID projectId;
    private UUID userId;
    private Project project;
    private User user;
    private ProjectRequest projectRequest;
    private CustomUserDetails customUserDetails;
    private Set<UUID> teamMemberIds;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.PROJECT_GROUP_MANAGER)
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

        teamMemberIds = new HashSet<>(Collections.singletonList(userId));

        projectRequest = ProjectRequest.builder()
                .title("Test Project")
                .description("Test Description")
                .department("IT")
                .teamMemberIds(teamMemberIds)
                .build();

        customUserDetails = new CustomUserDetails(user);
    }

    @Test
    void createProject_Success() {
        setupSecurityContext(customUserDetails);
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.createProject(projectRequest);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(projectRequest.getTitle(), response.getTitle());
        assertEquals(projectRequest.getDepartment(), response.getDepartment());
        assertEquals(ProjectStatus.IN_PROGRESS, response.getStatus());

        verify(userService).getUserEntityById(userId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void createProject_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        setupSecurityContext(differentUserDetails);

        assertThrows(DeniedAccessException.class,
                () -> projectService.createProject(projectRequest));

        verify(projectRepository, never()).save(any(Project.class));
    }

    private void setupSecurityContext(CustomUserDetails userDetails) {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getProjectById_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        ProjectResponse response = projectService.getProjectById(projectId);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(project.getTitle(), response.getTitle());
        assertEquals(project.getDepartment(), response.getDepartment());

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectById_NotFound() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectService.getProjectById(projectId));

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectsByDepartment_Success() {
        List<Project> projects = Collections.singletonList(project);
        when(projectRepository.findByDepartment("IT")).thenReturn(projects);

        List<ProjectResponse> responses = projectService.getProjectsByDepartment("IT");

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(projectId, responses.get(0).getId());
        assertEquals(project.getTitle(), responses.get(0).getTitle());

        verify(projectRepository).findByDepartment("IT");
    }

    @Test
    void updateProject_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.updateProject(projectId, projectRequest);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(projectRequest.getTitle(), response.getTitle());
        assertEquals(projectRequest.getDepartment(), response.getDepartment());

        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProjectStatus_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.updateProjectStatus(projectId, "COMPLETED");

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(ProjectStatus.COMPLETED, response.getStatus());

        verify(projectRepository).findById(projectId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void updateProjectStatus_InvalidTransition() {
        project.setStatus(ProjectStatus.COMPLETED);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThrows(InvalidStateTransitionException.class,
                () -> projectService.updateProjectStatus(projectId, "IN_PROGRESS"));

        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void addTeamMember_Success() {
        setupSecurityContext(customUserDetails);
        UUID newUserId = UUID.randomUUID();
        User newUser = User.builder()
                .id(newUserId)
                .department("IT")
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getUserEntityById(newUserId)).thenReturn(newUser);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.addTeamMember(projectId, newUserId);

        assertNotNull(response);
        assertEquals(projectId, response.getId());

        verify(projectRepository).findById(projectId);
        verify(userService).getUserEntityById(newUserId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void removeTeamMember_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userService.getUserEntityById(userId)).thenReturn(user);
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ProjectResponse response = projectService.removeTeamMember(projectId, userId);

        assertNotNull(response);
        assertEquals(projectId, response.getId());

        verify(projectRepository).findById(projectId);
        verify(userService).getUserEntityById(userId);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void deleteProject_Success() {
        setupSecurityContext(customUserDetails);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).delete(project);

        projectService.deleteProject(projectId);

        verify(projectRepository).findById(projectId);
        verify(projectRepository).delete(project);
    }

    @Test
    void getProjectByIdWithValidation_Success() {
        Project projectWithUser = Project.builder()
                .id(projectId)
                .title("Test Project")
                .description("Test Description")
                .department("IT")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(new HashSet<>(Collections.singletonList(user)))
                .build();
        
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectWithUser));

        ProjectResponse response = projectService.getProjectByIdWithValidation(projectId, userId);

        assertNotNull(response);
        assertEquals(projectId, response.getId());
        assertEquals(projectWithUser.getTitle(), response.getTitle());
        assertEquals(projectWithUser.getDepartment(), response.getDepartment());

        verify(projectRepository).findById(projectId);
    }

    @Test
    void getProjectByIdWithValidation_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .name("Different User")
                .build();

        Project projectWithoutUser = Project.builder()
                .id(projectId)
                .title("Test Project")
                .description("Test Description")
                .department("IT")
                .status(ProjectStatus.IN_PROGRESS)
                .teamMembers(new HashSet<>())
                .build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectWithoutUser));

        assertThrows(DeniedAccessException.class,
                () -> projectService.getProjectByIdWithValidation(projectId, differentUser.getId()));

        verify(projectRepository).findById(projectId);
    }
    
    @Test
    void getProjectEntityByIdWithValidation_Success() {
        when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.of(project));
        
        Project result = projectService.getProjectEntityByIdWithValidation(projectId, userId);
        
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        
        verify(projectRepository).findByIdAndUserAccess(projectId, userId);
    }
    
    @Test
    void getProjectEntityByIdWithValidation_NotFound() {
        when(projectRepository.findByIdAndUserAccess(projectId, userId)).thenReturn(Optional.empty());
        
        assertThrows(EntityNotFoundException.class, 
                () -> projectService.getProjectEntityByIdWithValidation(projectId, userId));
                
        verify(projectRepository).findByIdAndUserAccess(projectId, userId);
    }
    
    @Test
    void assignTask_Success() {
        setupSecurityContext(customUserDetails);
        UUID taskId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.assignTaskToProject(taskId, projectId)).thenReturn(1);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        
        ProjectResponse response = projectService.assignTask(projectId, taskId);
        
        assertNotNull(response);
        assertEquals(projectId, response.getId());
        
        verify(projectRepository, times(2)).findById(projectId);
        verify(taskRepository).assignTaskToProject(taskId, projectId);
    }
    
    @Test
    void assignTask_TaskNotFound() {
        setupSecurityContext(customUserDetails);
        UUID taskId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.assignTaskToProject(taskId, projectId)).thenReturn(0);
        
        assertThrows(EntityNotFoundException.class,
                () -> projectService.assignTask(projectId, taskId));
                
        verify(projectRepository).findById(projectId);
        verify(taskRepository).assignTaskToProject(taskId, projectId);
    }
}