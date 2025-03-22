package com.definex.task_management.service.impl;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.entity.Comment;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.repository.CommentRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.repository.UserRepository;
import com.definex.task_management.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CommentServiceImpl commentService;

    private UUID commentId;
    private UUID taskId;
    private UUID userId;
    private Task task;
    private User user;
    private Project project;
    private Comment comment;
    private CommentRequest commentRequest;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .role(UserRole.TEAM_MEMBER)
                .department("IT")
                .build();

        project = Project.builder()
                .id(UUID.randomUUID())
                .title("Test Project")
                .department("IT")
                .teamMembers(new HashSet<>(Collections.singletonList(user)))
                .build();

        task = Task.builder()
                .id(taskId)
                .title("Test Task")
                .project(project)
                .assignees(new HashSet<>(Collections.singletonList(user)))
                .build();

        comment = Comment.builder()
                .id(commentId)
                .content("Test comment")
                .task(task)
                .user(user)
                .build();

        commentRequest = CommentRequest.builder()
                .content("Test comment")
                .taskId(taskId)
                .build();

        customUserDetails = new CustomUserDetails(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createComment_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.createComment(commentRequest);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals(commentRequest.getContent(), response.getContent());
        assertEquals(taskId, response.getTaskId());
        assertEquals(userId, response.getUser().getId());

        verify(taskRepository).findById(taskId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_TaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentService.createComment(commentRequest));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void createComment_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(userRepository.findById(differentUser.getId())).thenReturn(Optional.of(differentUser));

        assertThrows(DeniedAccessException.class,
                () -> commentService.createComment(commentRequest));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void getCommentById_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.getCommentById(commentId);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals(comment.getContent(), response.getContent());
        assertEquals(taskId, response.getTaskId());
        assertEquals(userId, response.getUser().getId());

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentById_NotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentService.getCommentById(commentId));

        verify(commentRepository).findById(commentId);
    }

    @Test
    void getCommentsByTaskId_Success() {
        List<Comment> comments = Collections.singletonList(comment);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(commentRepository.findByTaskId(taskId)).thenReturn(comments);

        List<CommentResponse> responses = commentService.getCommentsByTaskId(taskId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(commentId, responses.get(0).getId());
        assertEquals(comment.getContent(), responses.get(0).getContent());
        assertEquals(taskId, responses.get(0).getTaskId());
        assertEquals(userId, responses.get(0).getUser().getId());

        verify(taskRepository).findById(taskId);
        verify(commentRepository).findByTaskId(taskId);
    }

    @Test
    void getCommentsByTaskId_TaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentService.getCommentsByTaskId(taskId));

        verify(taskRepository).findById(taskId);
        verify(commentRepository, never()).findByTaskId(any());
    }

    @Test
    void updateComment_Success() {
        String newContent = "Updated comment";
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponse response = commentService.updateComment(commentId, newContent);

        assertNotNull(response);
        assertEquals(commentId, response.getId());
        assertEquals(taskId, response.getTaskId());
        assertEquals(userId, response.getUser().getId());

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void updateComment_NotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentService.updateComment(commentId, "Updated comment"));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void updateComment_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));


        assertThrows(DeniedAccessException.class,
                () -> commentService.updateComment(commentId, "Updated comment"));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void deleteComment_Success() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(commentId);

        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_NotFound() {
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(DeniedAccessException.class,
                () -> commentService.deleteComment(commentId));

        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }
}