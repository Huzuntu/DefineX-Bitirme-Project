package com.definex.task_management.service.impl;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.ProjectStatus;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.InvalidStateTransitionException;
import com.definex.task_management.mapper.ProjectMapper;
import com.definex.task_management.repository.ProjectRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.BaseService;
import com.definex.task_management.service.ProjectService;
import com.definex.task_management.service.ProjectTaskService;
import com.definex.task_management.service.UserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ProjectServiceImpl extends BaseService implements ProjectService {
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final ProjectTaskService projectTaskService;

    public ProjectServiceImpl(ProjectRepository projectRepository, 
                            UserService userService,
                            ProjectTaskService projectTaskService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.projectTaskService = projectTaskService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        log.info("Creating new project with title: {}", projectRequest.getTitle());
        CustomUserDetails currentUser = getCurrentUser();
        validateSameDepartment(currentUser, projectRequest.getDepartment());

        Project project = ProjectMapper.toEntity(projectRequest);
        project.setStatus(ProjectStatus.IN_PROGRESS);

        if (projectRequest.getTeamMemberIds() != null && !projectRequest.getTeamMemberIds().isEmpty()) {
            Set<User> teamMembers = projectRequest.getTeamMemberIds().stream()
                    .map(id -> userService.getUserEntityById(id))
                    .collect(Collectors.toSet());
            project.setTeamMembers(teamMembers);
        }

        Project savedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    @Cacheable(value = "projectCache", key = "#projectId")
    public ProjectResponse getProjectById(UUID projectId) {
        log.info("Fetching project with id: {}", projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);
        return ProjectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    @Cacheable(value = "projectCache", key = "'department:' + #department")
    public List<ProjectResponse> getProjectsByDepartment(String department) {
        log.info("Fetching all projects for department: {}", department);
        CustomUserDetails currentUser = getCurrentUser();
        validateSameDepartment(currentUser, department);

        List<Project> projects = projectRepository.findByDepartment(department);
        return projects.stream()
                .map(ProjectMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public ProjectResponse updateProject(UUID projectId, ProjectRequest projectRequest) {
        log.info("Updating project with id: {}", projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);

        project.setTitle(projectRequest.getTitle());
        project.setDescription(projectRequest.getDescription());
        project.setDepartment(projectRequest.getDepartment());

        if (projectRequest.getTeamMemberIds() != null) {
            Set<User> teamMembers = projectRequest.getTeamMemberIds().stream()
                    .map(id -> userService.getUserEntityById(id))
                    .collect(Collectors.toSet());
            project.setTeamMembers(teamMembers);
        }

        Project updatedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public ProjectResponse updateProjectStatus(UUID projectId, String newStatus) {
        log.info("Updating status of project id: {} to: {}", projectId, newStatus);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);
        ProjectStatus projectStatus = ProjectStatus.valueOf(newStatus);
        if (!project.getStatus().canTransitionTo(projectStatus)) {
            throw new InvalidStateTransitionException("Invalid status transition from " + project.getStatus() + " to " + newStatus);
        }

        project.setStatus(projectStatus);
        Project updatedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER')")
    public ProjectResponse assignTask(UUID projectId, UUID taskId) {
        log.info("Assigning task id: {} to project id: {}", taskId, projectId);
        return projectTaskService.assignTaskToProject(projectId, taskId, getCurrentUser());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public ProjectResponse addTeamMember(UUID projectId, UUID userId) {
        log.info("Adding user id: {} to project id: {}", userId, projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);

        User user = userService.getUserEntityById(userId);

        project.getTeamMembers().add(user);
        Project updatedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public ProjectResponse removeTeamMember(UUID projectId, UUID userId) {
        log.info("Removing user id: {} from project id: {}", userId, projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);

        User user = userService.getUserEntityById(userId);

        project.getTeamMembers().remove(user);
        Project updatedProject = projectRepository.save(project);
        return ProjectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    @CacheEvict(value = "projectCache", allEntries = true)
    public void deleteProject(UUID projectId) {
        log.info("Deleting project with id: {}", projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(currentUser, project);
        projectRepository.delete(project);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ProjectResponse getProjectByIdWithValidation(UUID projectId, UUID userId) {
        Project project = getProjectEntityById(projectId);
        boolean isAssigned = project.getTeamMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));
        if (!isAssigned) {
            throw new DeniedAccessException("User is not assigned to this project");
        }
        
        return ProjectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public Project getProjectEntityByIdWithValidation(UUID projectId, UUID userId) {
        return projectTaskService.getProjectEntityByIdWithValidation(projectId, userId);
    }

    private Project getProjectEntityById(UUID projectId) {
        return projectTaskService.getProjectEntityById(projectId);
    }

    private void validateStateTransition(ProjectStatus currentState, ProjectStatus newState) {
        if (!currentState.canTransitionTo(newState)) {
            throw new InvalidStateTransitionException("Invalid state transition from " + currentState + " to " + newState);
        }
    }
}