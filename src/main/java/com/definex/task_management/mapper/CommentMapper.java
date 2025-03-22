package com.definex.task_management.mapper;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.entity.Comment;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public static Comment toEntity(CommentRequest request, Task task, User user) {
        return Comment.builder()
                .content(request.getContent())
                .task(task)
                .user(user)
                .build();
    }

    public static CommentResponse toResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .taskId(comment.getTask().getId())
                .user(UserMapper.toResponse(comment.getUser()))
                .deleted(comment.isDeleted())
                .build();
    }

    public static void updateEntity(Comment comment, String newContent) {
        comment.setContent(newContent);
    }
}