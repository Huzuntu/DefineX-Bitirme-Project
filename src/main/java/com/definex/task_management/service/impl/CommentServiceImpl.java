package com.definex.task_management.service.impl;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.entity.Comment;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.CommentMapper;
import com.definex.task_management.repository.CommentRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.repository.UserRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.CommentService;
import com.definex.task_management.service.BaseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CommentServiceImpl extends BaseService implements CommentService {
    private final CommentRepository commentRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public CommentServiceImpl(CommentRepository commentRepository, TaskRepository taskRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public CommentResponse createComment(CommentRequest commentRequest) {
        log.info("Creating new comment for task id: {}", commentRequest.getTaskId());
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(commentRequest.getTaskId());
        User user = getUserEntityById(currentUser.getUserId());

        validateUserAccessToProject(currentUser, task.getProject());
        validateUserAccessToTask(currentUser, task);

        Comment comment = CommentMapper.toEntity(commentRequest, task, user);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toResponse(savedComment);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public CommentResponse getCommentById(UUID commentId) {
        log.info("Fetching comment with id: {}", commentId);
        CustomUserDetails currentUser = getCurrentUser();
        Comment comment = getCommentEntityById(commentId);

        validateUserAccessToProject(currentUser, comment.getTask().getProject());
        validateUserAccessToTask(currentUser, comment.getTask());

        return CommentMapper.toResponse(comment);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public List<CommentResponse> getCommentsByTaskId(UUID taskId) {
        log.info("Fetching all comments for task id: {}", taskId);
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);

        validateUserAccessToProject(currentUser, task.getProject());
        validateUserAccessToTask(currentUser, task);

        List<Comment> comments = commentRepository.findByTaskId(taskId);
        return comments.stream()
                .map(CommentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public CommentResponse updateComment(UUID commentId, String content) {
        log.info("Updating comment with id: {}", commentId);
        CustomUserDetails currentUser = getCurrentUser();
        Comment comment = getCommentEntityById(commentId);

        validateUserAccessToProject(currentUser, comment.getTask().getProject());
        validateUserAccessToTask(currentUser, comment.getTask());

        if (!comment.getUser().getId().equals(currentUser.getUserId()) &&
                !currentUser.getRole().equals(UserRole.PROJECT_GROUP_MANAGER) &&
                !currentUser.getRole().equals(UserRole.PROJECT_MANAGER) &&
                !currentUser.getRole().equals(UserRole.TEAM_LEADER)) {
            throw new DeniedAccessException("You can only update your own comments");
        }

        CommentMapper.updateEntity(comment, content);
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toResponse(updatedComment);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public void deleteComment(UUID commentId) {
        log.info("Deleting comment with id: {}", commentId);
        CustomUserDetails currentUser = getCurrentUser();
        Comment comment = getCommentEntityById(commentId);

        validateUserAccessToProject(currentUser, comment.getTask().getProject());
        validateUserAccessToTask(currentUser, comment.getTask());

        if (!comment.getUser().getId().equals(currentUser.getUserId()) &&
                !currentUser.getRole().equals(UserRole.PROJECT_GROUP_MANAGER) &&
                !currentUser.getRole().equals(UserRole.PROJECT_MANAGER) &&
                !currentUser.getRole().equals(UserRole.TEAM_LEADER)) {
            throw new DeniedAccessException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    private Comment getCommentEntityById(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found with id: " + commentId));
    }

    private Task getTaskEntityById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
    }

    private User getUserEntityById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
    }

    private void validateUserAccessToTask(CustomUserDetails currentUser, Task task) {
        if (currentUser.getRole().equals(UserRole.PROJECT_GROUP_MANAGER) ||
                currentUser.getRole().equals(UserRole.PROJECT_MANAGER) ||
                currentUser.getRole().equals(UserRole.TEAM_LEADER)) {
            return;
        }
        if (currentUser.getRole().equals(UserRole.TEAM_MEMBER)) {
            boolean isAssigned = task.getAssignees().stream()
                    .anyMatch(assignee -> assignee.getId().equals(currentUser.getUserId()));
            if (!isAssigned) {
                throw new DeniedAccessException("You can only access comments for tasks you are assigned to");
            }
        }
    }
} 