package com.definex.task_management.dto;

import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private UUID id;
    private String title;
    private String userStory;
    private String acceptanceCriteria;
    private TaskState state;
    private TaskPriority priority;
    private UUID projectId;
    private String projectTitle;
    private Set<UserResponse> assignees;
    private List<CommentResponse> comments;
    private List<AttachmentResponse> attachments;
} 