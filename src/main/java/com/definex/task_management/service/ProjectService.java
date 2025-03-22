package com.definex.task_management.service;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.entity.Project;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    public ProjectResponse createProject(ProjectRequest projectRequest);
    public ProjectResponse getProjectById(UUID projectId);
    public List<ProjectResponse> getProjectsByDepartment(String department);
    public ProjectResponse updateProject(UUID projectId, ProjectRequest projectRequest);
    public ProjectResponse updateProjectStatus(UUID projectId, String newStatus);
    public ProjectResponse assignTask(UUID projectId, UUID taskId);
    public ProjectResponse addTeamMember(UUID projectId, UUID userId);
    public ProjectResponse removeTeamMember(UUID projectId, UUID userId);
    public void deleteProject(UUID projectId);

    ProjectResponse getProjectByIdWithValidation(UUID projectId, UUID userId);
    Project getProjectEntityByIdWithValidation(UUID projectId, UUID userId);
}
