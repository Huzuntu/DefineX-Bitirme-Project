package com.definex.task_management.controller;

import com.definex.task_management.dto.CommentRequest;
import com.definex.task_management.dto.CommentResponse;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.Comment;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CommentControllerTest {
    private static final String API_BASE_PATH = "/api/v1/comments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    ObjectMapper objectMapper = new ObjectMapper();

    private UUID commentId;
    private UUID taskId;
    private UUID userId;
    private CommentResponse commentResponse;
    private List<CommentResponse> comments;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        userId = UUID.randomUUID();

        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .name("test")
                .email("test@definex.com")
                .department("Engineering")
                .role(UserRole.TEAM_MEMBER)
                .build();

        commentResponse = CommentResponse.builder()
                .id(commentId)
                .content("Test Comment")
                .taskId(taskId)
                .user(userResponse)
                .build();

        comments = List.of(commentResponse);

        comment = Comment.builder()
                .id(commentId)
                .build();

        commentRequest = CommentRequest.builder()
                .content("Test Comment")
                .taskId(taskId)
                .build();
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void createComment_ShouldReturnOk() throws Exception {
        when(commentService.createComment(any(CommentRequest.class))).thenReturn(commentResponse);

        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"content\":\"Test Comment\",\"taskId\":\"" + taskId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void createComment_ShouldReturnBadRequest() throws Exception {
        CommentRequest invalidRequest = CommentRequest.builder()
                .content("")
                .build();
        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void createComment_ShouldReturnNotFound() throws Exception {
        when(commentService.createComment(any(CommentRequest.class)))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void createComment_ShouldReturnForbidden() throws Exception {
        when(commentService.createComment(any(CommentRequest.class)))
                .thenThrow(new DeniedAccessException("Access denied"));

        mockMvc.perform(post(API_BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentById_ShouldReturnOk() throws Exception {
        when(commentService.getCommentById(commentId)).thenReturn(commentResponse);

        mockMvc.perform(get(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentById_ShouldReturnNotFound() throws Exception {
        when(commentService.getCommentById(commentId))
                .thenThrow(new EntityNotFoundException("Comment not found with id: " + commentId));

        mockMvc.perform(get(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentById_ShouldReturnForbidden() throws Exception {
        when(commentService.getCommentById(commentId))
                .thenThrow(new DeniedAccessException("Access denied"));

        mockMvc.perform(get(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentsByTaskId_ShouldReturnOk() throws Exception {
        when(commentService.getCommentsByTaskId(taskId)).thenReturn(comments);

        mockMvc.perform(get(API_BASE_PATH + "/task/" + taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commentId.toString()))
                .andExpect(jsonPath("$[0].content").value("Test Comment"))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentsByTaskId_ShouldReturnNotFound() throws Exception {
        when(commentService.getCommentsByTaskId(taskId))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(get(API_BASE_PATH + "/task/" + taskId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void getCommentsByTaskId_ShouldReturnForbidden() throws Exception {
        when(commentService.getCommentsByTaskId(taskId))
                .thenThrow(new DeniedAccessException("Access denied"));

        mockMvc.perform(get(API_BASE_PATH + "/task/" + taskId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void updateComment_ShouldReturnOk() throws Exception {
        when(commentService.updateComment(eq(commentId), any(String.class))).thenReturn(commentResponse);

        mockMvc.perform(put(API_BASE_PATH + "/" + commentId)
                .param("content", "Updated Comment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.content").value("Test Comment"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void updateComment_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(put(API_BASE_PATH + "/" + commentId)
                .param("content", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failure"));
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void updateComment_ShouldReturnForbidden() throws Exception {
        when(commentService.updateComment(eq(commentId), any(String.class)))
                .thenThrow(new DeniedAccessException("Access denied"));

        mockMvc.perform(put(API_BASE_PATH + "/" + commentId)
                .param("content", "Updated Comment"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void deleteComment_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void deleteComment_ShouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Comment not found with id: " + commentId))
                .when(commentService).deleteComment(commentId);

        mockMvc.perform(delete(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"TEAM_MEMBER"})
    void deleteComment_ShouldReturnForbidden() throws Exception {
        doThrow(new DeniedAccessException("Access denied"))
                .when(commentService).deleteComment(commentId);

        mockMvc.perform(delete(API_BASE_PATH + "/" + commentId))
                .andExpect(status().isForbidden());
    }
}
