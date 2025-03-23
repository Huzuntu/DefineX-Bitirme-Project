package com.definex.task_management.service.impl;

import com.definex.task_management.dto.TaskRequest;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.exception.InvalidStateTransitionException;
import com.definex.task_management.mapper.TaskMapper;
import com.definex.task_management.repository.ProjectRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.BaseService;
import com.definex.task_management.service.TaskService;
import com.definex.task_management.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TaskServiceImpl extends BaseService implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public TaskServiceImpl(TaskRepository taskRepository,
                          ProjectRepository projectRepository,
                          UserService userService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse createTask(TaskRequest taskRequest) {
        log.info("Creating new task with title: {}", taskRequest.getTitle());
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityByIdWithValidation(taskRequest.getProjectId(), currentUser.getUserId());

        Task task = TaskMapper.toEntity(taskRequest);
        task.setProject(project);
        task.setState(TaskState.BACKLOG);

        if (taskRequest.getAssigneeIds() != null && !taskRequest.getAssigneeIds().isEmpty()) {
            Set<User> assignees = new HashSet<>();
            for (UUID id : taskRequest.getAssigneeIds()) {
                assignees.add(userService.getUserEntityById(id));
            }
            task.setAssignees(assignees);
        }
        Task savedTask = taskRepository.save(task);
        
        return TaskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskCache", key = "#taskId")
    public TaskResponse getTaskById(UUID taskId) {
        log.info("Fetching task with id: {}", taskId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());
        return TaskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "taskCache", key = "'project:' + #projectId")
    public List<TaskResponse> getAllTasksUnderProject(UUID projectId) {
        log.info("Fetching all tasks for project id: {}", projectId);
        CustomUserDetails currentUser = getCurrentUser();
        Project project = getProjectEntityByIdWithValidation(projectId, currentUser.getUserId());

        validateProjectAccess(currentUser, project);

        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream()
                .map(TaskMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse updateTask(UUID taskId, TaskRequest taskRequest) {
        log.info("Updating task with id: {}", taskId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());

        if (!task.getProject().getId().equals(taskRequest.getProjectId())) {
            throw new DeniedAccessException("Cannot change task's project");
        }

        task.setTitle(taskRequest.getTitle());
        task.setUserStory(taskRequest.getUserStory());
        task.setAcceptanceCriteria(taskRequest.getAcceptanceCriteria());
        task.setPriority(taskRequest.getPriority());

        if (taskRequest.getAssigneeIds() != null) {
            Set<User> assignees = taskRequest.getAssigneeIds().stream()
                    .map(id -> userService.getUserEntityById(id))
                    .collect(Collectors.toSet());
            task.setAssignees(assignees);
        }
        Task updatedTask = taskRepository.save(task);
        return TaskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse deleteTask(UUID taskId) {
        log.info("Deleting task with id: {}", taskId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());
        taskRepository.delete(task);
        return TaskMapper.toResponse(task);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse updateTaskState(UUID taskId, TaskState newState, String reason) {
        log.info("Updating task state for task id: {} to state: {}", taskId, newState);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());
        validateStateTransition(task.getState(), newState, reason);

        task.setState(newState);
        if (newState == TaskState.BLOCKED || newState == TaskState.CANCELLED) {
            task.setStateTransitionReason(reason);
        }

        Task updatedTask = taskRepository.save(task);
        return TaskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse updateTaskPriority(UUID taskId, TaskPriority priority) {
        log.info("Updating task priority for task id: {} to priority: {}", taskId, priority);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);

        validateProjectAccess(currentUser, task.getProject());

        task.setPriority(priority);
        Task updatedTask = taskRepository.save(task);
        return TaskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse assignUserToTask(UUID taskId, UUID userId) {
        log.info("Assigning user id: {} to task id: {}", userId, taskId);
        User user = userService.getUserEntityById(userId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());

        task.getAssignees().add(user);
        Task updatedTask = taskRepository.save(task);
        return TaskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    @CacheEvict(value = "taskCache", allEntries = true)
    public TaskResponse removeUserFromTask(UUID taskId, UUID userId) {
        log.info("Removing user id: {} from task id: {}", userId, taskId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);
        validateProjectAccess(currentUser, task.getProject());

        User user = userService.getUserEntityById(userId);
        boolean isAssigned = task.getAssignees().stream()
            .anyMatch(assignee -> assignee.getId().equals(userId));
        if (!isAssigned) {
            throw new EntityNotFoundException("User is not assigned to this task");
        }

        task.getAssignees().remove(user);
        Task updatedTask = taskRepository.save(task);
        return TaskMapper.toResponse(updatedTask);
    }

    private Task getTaskEntityById(UUID taskId) {
        log.info("Fetching task entity with id: {}", taskId);
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
    }

    private void validateStateTransition(TaskState currentState, TaskState newState, String reason) {
        if (!currentState.canTransitionTo(newState, reason)) {
            throw new InvalidStateTransitionException("Invalid state transition from " + currentState + " to " + newState);
        }
    }
    
    private void validateProjectAccess(CustomUserDetails currentUser, Project project) {
        log.info("Validating project access for user: {}", currentUser.getUsername());
        validateUserAccessToProject(currentUser, project);
    }
    
    private Project getProjectEntityByIdWithValidation(UUID projectId, UUID userId) {
        log.info("Fetching project entity with id: {} and validating for user id: {}", projectId, userId);
        return projectRepository.findByIdAndUserAccess(projectId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + projectId + 
                                " or user doesn't have access"));
    }
} 