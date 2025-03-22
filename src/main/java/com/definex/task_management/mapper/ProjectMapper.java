package com.definex.task_management.mapper;

import com.definex.task_management.dto.ProjectRequest;
import com.definex.task_management.dto.ProjectResponse;
import com.definex.task_management.entity.Project;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public static Project toEntity(ProjectRequest request) {
        return Project.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .department(request.getDepartment())
                .teamMembers(new HashSet<>())
                .tasks(new HashSet<>())
                .build();
    }

    public static ProjectResponse toResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .department(project.getDepartment())
                .status(project.getStatus())
                .teamMembers(project.getTeamMembers().stream()
                        .map(UserMapper::toResponse)
                        .collect(Collectors.toSet()))
                .tasks(project.getTasks().stream()
                        .map(TaskMapper::toResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public static void updateEntity(Project project, ProjectRequest request) {
        project.setTitle(request.getTitle());
        project.setDescription(request.getDescription());
        project.setDepartment(request.getDepartment());
    }
}