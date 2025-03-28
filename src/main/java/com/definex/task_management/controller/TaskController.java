package com.definex.task_management.controller;

import com.definex.task_management.dto.TaskRequest;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.dto.TaskStateUpdateRequest;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "4. Task Management")
@Slf4j
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        log.info("Creating new task");
        return ResponseEntity.ok(taskService.createTask(taskRequest));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable UUID taskId) {
        log.info("Fetching task with id: {}", taskId);
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<List<TaskResponse>> getAllTasksUnderProject(@PathVariable UUID projectId) {
        log.info("Fetching all tasks for project id: {}", projectId);
        return ResponseEntity.ok(taskService.getAllTasksUnderProject(projectId));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable UUID taskId, @Valid @RequestBody TaskRequest taskRequest) {
        log.info("Updating task with id: {}", taskId);
        return ResponseEntity.ok(taskService.updateTask(taskId, taskRequest));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> deleteTask(@PathVariable UUID taskId) {
        log.info("Deleting task with id: {}", taskId);
        return ResponseEntity.ok(taskService.deleteTask(taskId));
    }

    @PatchMapping("/{taskId}/state")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<TaskResponse> updateTaskState(
            @PathVariable UUID taskId,
            @Valid @RequestBody TaskStateUpdateRequest request) {
        log.info("Updating task state for task id: {} to state: {}", taskId, request.getNewState());
        return ResponseEntity.ok(taskService.updateTaskState(taskId, request.getNewState(), request.getReason()));
    }

    @PatchMapping("/{taskId}/priority")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> updateTaskPriority(
            @PathVariable UUID taskId,
            @RequestParam TaskPriority priority) {
        log.info("Updating task priority for task id: {} to priority: {}", taskId, priority);
        return ResponseEntity.ok(taskService.updateTaskPriority(taskId, priority));
    }

    @PostMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> assignUserToTask(
            @PathVariable UUID taskId,
            @PathVariable UUID userId) {
        log.info("Assigning user id: {} to task id: {}", userId, taskId);
        return ResponseEntity.ok(taskService.assignUserToTask(taskId, userId));
    }

    @DeleteMapping("/{taskId}/assignees/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER')")
    public ResponseEntity<TaskResponse> removeUserFromTask(
            @PathVariable UUID taskId,
            @PathVariable UUID userId) {
        log.info("Removing user id: {} from task id: {}", userId, taskId);
        return ResponseEntity.ok(taskService.removeUserFromTask(taskId, userId));
    }
} 