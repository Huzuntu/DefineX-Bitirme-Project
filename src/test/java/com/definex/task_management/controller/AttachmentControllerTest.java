package com.definex.task_management.controller;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.entity.Attachment;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AttachmentControllerTest {
    private static final String API_BASE_PATH = "/api/v1/attachments";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    private UUID attachmentId;
    private UUID taskId;
    private AttachmentResponse attachmentResponse;
    private MockMultipartFile file;
    private List<AttachmentResponse> attachments;
    private Attachment attachment;

    @BeforeEach
    void setUp() throws Exception {
        
        attachmentId = UUID.randomUUID();
        taskId = UUID.randomUUID();

        attachmentResponse = AttachmentResponse.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .fileType("text/plain")
                .fileSize(100L)
                .taskId(taskId)
                .uploadDate(LocalDateTime.now())
                .build();

        file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Hello, World!".getBytes()
        );

        attachments = List.of(attachmentResponse);

        attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .filePath("path/to/test.txt")
                .build();
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void uploadAttachment_ShouldReturnOk() throws Exception {
        when(attachmentService.uploadAttachment(any(AttachmentRequest.class)))
                .thenReturn(attachmentResponse);

        mockMvc.perform(multipart(API_BASE_PATH)
                .file(file)
                .param("taskId", taskId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void uploadAttachment_ShouldReturnForbidden() throws Exception {
        when(attachmentService.uploadAttachment(any(AttachmentRequest.class)))
                .thenThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"));

        mockMvc.perform(multipart(API_BASE_PATH)
                .file(file)
                .param("taskId", taskId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachmentById_ShouldReturnOk() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId))
                .thenReturn(attachmentResponse);

        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attachmentId.toString()))
                .andExpect(jsonPath("$.fileName").value("test.txt"))
                .andExpect(jsonPath("$.taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachmentById_ShouldReturnNotFound() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId))
                .thenThrow(new EntityNotFoundException("Attachment not found with id: " + attachmentId));

        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attachment not found with id: " + attachmentId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void getAttachmentById_ShouldReturnForbidden() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId))
                .thenThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"));

        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachments_ShouldReturnOk() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenReturn(attachments);

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("test.txt"))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachments_ShouldReturnNotFound() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void getAttachments_ShouldReturnForbidden() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"));

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachmentsByTaskId_ShouldReturnOk() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenReturn(attachments);

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(attachmentId.toString()))
                .andExpect(jsonPath("$[0].fileName").value("test.txt"))
                .andExpect(jsonPath("$[0].taskId").value(taskId.toString()));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void getAttachmentsByTaskId_ShouldReturnNotFound() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenThrow(new EntityNotFoundException("Task not found with id: " + taskId));

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: " + taskId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void getAttachmentsByTaskId_ShouldReturnForbidden() throws Exception {
        when(attachmentService.getAttachmentsByTaskId(taskId))
                .thenThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"));

        mockMvc.perform(get(API_BASE_PATH + "/task/{taskId}", taskId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void deleteAttachment_ShouldReturnOk() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(attachmentId);

        mockMvc.perform(delete(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void deleteAttachment_ShouldReturnNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Attachment not found with id: " + attachmentId))
                .when(attachmentService).deleteAttachment(attachmentId);

        mockMvc.perform(delete(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attachment not found with id: " + attachmentId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void deleteAttachment_ShouldReturnForbidden() throws Exception {
        doThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"))
                .when(attachmentService).deleteAttachment(attachmentId);

        mockMvc.perform(delete(API_BASE_PATH + "/{attachmentId}", attachmentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void downloadAttachment_ShouldReturnOk() throws Exception {
        when(attachmentService.getAttachmentById(attachmentId)).thenReturn(attachmentResponse);

        Resource mockResource = mock(Resource.class);
        when(mockResource.exists()).thenReturn(true);
        when(mockResource.isReadable()).thenReturn(true);
        when(mockResource.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
        
        when(attachmentService.downloadAttachment(attachmentId)).thenReturn(mockResource);
        
        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}/download", attachmentId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_PROJECT_GROUP_MANAGER", "Engineering"})
    void downloadAttachment_ShouldReturnNotFound() throws Exception {
        when(attachmentService.downloadAttachment(attachmentId))
                .thenThrow(new EntityNotFoundException("Attachment not found with id: " + attachmentId));

        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}/download", attachmentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Attachment not found with id: " + attachmentId));
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_TEAM_MEMBER", "IT"})
    void downloadAttachment_ShouldReturnForbidden() throws Exception {
        when(attachmentService.downloadAttachment(attachmentId))
                .thenThrow(new DeniedAccessException("You can only access attachments for tasks you are assigned to"));

        mockMvc.perform(get(API_BASE_PATH + "/{attachmentId}/download", attachmentId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You can only access attachments for tasks you are assigned to"));
    }
}
