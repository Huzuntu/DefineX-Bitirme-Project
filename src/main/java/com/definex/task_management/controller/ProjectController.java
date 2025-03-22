package com.definex.task_management.controller;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.createProject(projectRequest));
    }

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<List<ProjectResponse>> getProjectsByDepartment(@PathVariable String department) {
        return ResponseEntity.ok(projectService.getProjectsByDepartment(department));
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody ProjectRequest projectRequest) {
        return ResponseEntity.ok(projectService.updateProject(projectId, projectRequest));
    }

    @PatchMapping("/{projectId}/status/{status}")
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ProjectResponse> updateProjectStatus(
            @PathVariable UUID projectId,
            @PathVariable String status) {
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, status));
    }

    @PostMapping("/{projectId}/tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER')")
    public ResponseEntity<ProjectResponse> assignTask(
            @PathVariable UUID projectId,
            @PathVariable UUID taskId) {
        return ResponseEntity.ok(projectService.assignTask(projectId, taskId));
    }

    @PostMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ProjectResponse> addTeamMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(projectService.addTeamMember(projectId, userId));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<ProjectResponse> removeTeamMember(
            @PathVariable UUID projectId,
            @PathVariable UUID userId) {
        return ResponseEntity.ok(projectService.removeTeamMember(projectId, userId));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_GROUP_MANAGER')")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.ok().build();
    }
} 