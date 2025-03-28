package com.definex.task_management.controller;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.service.CommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "5. Comment System")
@Slf4j
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<CommentResponse> createComment(@Valid @RequestBody CommentRequest commentRequest) {
        log.info("Creating new comment for task id: {}", commentRequest.getTaskId());
        return ResponseEntity.ok(commentService.createComment(commentRequest));
    }

    @GetMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<CommentResponse> getCommentById(@PathVariable UUID commentId) {
        log.info("Fetching comment with id: {}", commentId);
        return ResponseEntity.ok(commentService.getCommentById(commentId));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<List<CommentResponse>> getCommentsByTaskId(@PathVariable UUID taskId) {
        log.info("Fetching all comments for task id: {}", taskId);
        return ResponseEntity.ok(commentService.getCommentsByTaskId(taskId));
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable UUID commentId,
            @NotBlank @RequestParam String content) {
        log.info("Updating comment with id: {}", commentId);
        return ResponseEntity.ok(commentService.updateComment(commentId, content));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        log.info("Deleting comment with id: {}", commentId);
        commentService.deleteComment(commentId);
        return ResponseEntity.ok().build();
    }
} 