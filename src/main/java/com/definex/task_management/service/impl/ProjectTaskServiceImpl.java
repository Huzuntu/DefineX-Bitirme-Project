package com.definex.task_management.service.impl;

import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.ProjectMapper;
import com.definex.task_management.repository.ProjectRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.BaseService;
import com.definex.task_management.service.ProjectTaskService;
import com.definex.task_management.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
public class ProjectTaskServiceImpl extends BaseService implements ProjectTaskService {
    private final ProjectRepository projectRepository;
    private final TaskService taskService;

    public ProjectTaskServiceImpl(ProjectRepository projectRepository, TaskService taskService) {
        this.projectRepository = projectRepository;
        this.taskService = taskService;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER')")
    public ProjectResponse assignTaskToProject(UUID projectId, UUID taskId, CustomUserDetails currentUser) {
        log.info("Assigning task id: {} to project id: {}", taskId, projectId);
        Project project = getProjectEntityById(projectId);
        validateProjectAccess(currentUser, project);

        TaskResponse taskResponse = taskService.getTaskById(taskId);
        Task task = Task.builder()
                .id(taskResponse.getId())
                .title(taskResponse.getTitle())
                .userStory(taskResponse.getUserStory())
                .acceptanceCriteria(taskResponse.getAcceptanceCriteria())
                .state(taskResponse.getState())
                .priority(taskResponse.getPriority())
                .project(project)
                .build();

        project.getTasks().add(task);
        return ProjectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    public void validateProjectAccess(CustomUserDetails currentUser, Project project) {
        log.info("Validating project access for user: {}", currentUser.getUsername());
        validateUserAccessToProject(currentUser, project);
    }

    @Override
    @Transactional(readOnly = true)
    public Project getProjectEntityById(UUID projectId) {
        log.info("Fetching project entity with id: {}", projectId);
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public Project getProjectEntityByIdWithValidation(UUID projectId, UUID userId) {
        log.info("Fetching project entity with id: {} and validating for user id: {}", projectId, userId);
        Project project = getProjectEntityById(projectId);
        validateUserAccessToProject(getCurrentUser(), project);
        return project;
    }
}