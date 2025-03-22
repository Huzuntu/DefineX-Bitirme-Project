package com.definex.task_management.service;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;

import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponse createComment(CommentRequest commentRequest);
    CommentResponse getCommentById(UUID commentId);
    List<CommentResponse> getCommentsByTaskId(UUID taskId);
    CommentResponse updateComment(UUID commentId, String content);
    void deleteComment(UUID commentId);
} 