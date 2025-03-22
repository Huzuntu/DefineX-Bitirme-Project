package com.definex.task_management.service;

import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.security.CustomUserDetails;

import java.util.UUID;

public interface ProjectTaskService {
    ProjectResponse assignTaskToProject(UUID projectId, UUID taskId, CustomUserDetails currentUser);
    void validateProjectAccess(CustomUserDetails currentUser, Project project);
    Project getProjectEntityById(UUID projectId);
    Project getProjectEntityByIdWithValidation(UUID projectId, UUID userId);
} 