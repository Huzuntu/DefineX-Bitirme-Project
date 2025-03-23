package com.definex.task_management.service;

import com.definex.task_management.entity.Project;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public abstract class BaseService {
    protected CustomUserDetails getCurrentUser() {
        return (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    protected void validateSameDepartment(CustomUserDetails user, String department) {
        if (!user.getDepartment().equals(department)) {
            throw new DeniedAccessException("User does not have access to resources in another department");
        }
    }

    protected void validateUserAccessToProject(CustomUserDetails user, Project project) {
        validateSameDepartment(user, project.getDepartment());
        if (user.getRole() != UserRole.PROJECT_GROUP_MANAGER) {
            validateUserAssignedToProject(user.getUserId(), project);
        }
    }

    protected void validateUserAssignedToProject(UUID userId, Project project) {
        boolean isAssigned = project.getTeamMembers().stream()
                .anyMatch(member -> member.getId().equals(userId));

        if (!isAssigned) {
            throw new DeniedAccessException("User is not assigned to this project");
        }
    }

} 