package com.definex.task_management.dto;

import com.definex.task_management.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {
    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "User story is required")
    private String userStory;

    @NotBlank(message = "Acceptance criteria is required")
    private String acceptanceCriteria;

    @NotNull(message = "Priority is required")
    private TaskPriority priority;

    @NotNull(message = "Project ID is required")
    private UUID projectId;

    private Set<UUID> assigneeIds;
} 