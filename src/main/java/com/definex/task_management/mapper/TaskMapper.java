package com.definex.task_management.mapper;

import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.dto.TaskRequest;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.Attachment;
import com.definex.task_management.entity.Comment;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Component
public class TaskMapper {

    public static Task toEntity(TaskRequest request) {
        return Task.builder()
                .title(request.getTitle())
                .userStory(request.getUserStory())
                .acceptanceCriteria(request.getAcceptanceCriteria())
                .priority(request.getPriority())
                .assignees(ConcurrentHashMap.newKeySet())
                .build();
    }

    public static TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .userStory(task.getUserStory())
                .acceptanceCriteria(task.getAcceptanceCriteria())
                .state(task.getState())
                .priority(task.getPriority())
                .projectId(task.getProject().getId())
                .projectTitle(task.getProject().getTitle())
                .assignees(task.getAssignees().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toSet()))
                .comments(task.getComments().stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList()))
                .attachments(task.getAttachments().stream()
                .map(AttachmentMapper::toResponse)
                .collect(Collectors.toList()))
                .build();
    }
}
