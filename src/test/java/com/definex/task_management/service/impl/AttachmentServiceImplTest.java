package com.definex.task_management.service.impl;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.entity.Attachment;
import com.definex.task_management.entity.Project;
import com.definex.task_management.entity.Task;
import com.definex.task_management.entity.User;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.repository.AttachmentRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.security.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @TempDir
    Path tempDir;

    private AttachmentServiceImpl attachmentService;

    private UUID attachmentId;
    private UUID taskId;
    private UUID userId;
    private Task task;
    private User user;
    private Project project;
    private Attachment attachment;
    private AttachmentRequest attachmentRequest;
    private MockMultipartFile multipartFile;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        attachmentService = new AttachmentServiceImpl(attachmentRepository, taskRepository, tempDir.toString());

        attachmentId = UUID.randomUUID();
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

        attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .fileType("text/plain")
                .filePath(tempDir.resolve("test.txt").toString())
                .fileSize(100L)
                .task(task)
                .uploadDate(LocalDateTime.now())
                .build();

        multipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "test content".getBytes()
        );

        attachmentRequest = AttachmentRequest.builder()
                .file(multipartFile)
                .taskId(taskId)
                .build();

        customUserDetails = new CustomUserDetails(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(customUserDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void uploadAttachment_Success() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        AttachmentResponse response = attachmentService.uploadAttachment(attachmentRequest);

        assertNotNull(response);
        assertEquals(attachmentId, response.getId());
        assertEquals("test.txt", response.getFileName());
        assertEquals(taskId, response.getTaskId());

        verify(taskRepository).findById(taskId);
        verify(attachmentRepository).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_TaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.uploadAttachment(attachmentRequest));

        verify(taskRepository).findById(taskId);
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void uploadAttachment_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        assertThrows(DeniedAccessException.class,
                () -> attachmentService.uploadAttachment(attachmentRequest));

        verify(taskRepository).findById(taskId);
        verify(attachmentRepository, never()).save(any(Attachment.class));
    }

    @Test
    void getAttachmentById_Success() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        AttachmentResponse response = attachmentService.getAttachmentById(attachmentId);

        assertNotNull(response);
        assertEquals(attachmentId, response.getId());
        assertEquals("test.txt", response.getFileName());
        assertEquals(taskId, response.getTaskId());

        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void getAttachmentById_NotFound() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.getAttachmentById(attachmentId));

        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void getAttachmentsByTaskId_Success() {
        List<Attachment> attachments = Collections.singletonList(attachment);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(attachmentRepository.findByTaskId(taskId)).thenReturn(attachments);

        List<AttachmentResponse> responses = attachmentService.getAttachmentsByTaskId(taskId);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(attachmentId, responses.get(0).getId());
        assertEquals("test.txt", responses.get(0).getFileName());
        assertEquals(taskId, responses.get(0).getTaskId());

        verify(taskRepository).findById(taskId);
        verify(attachmentRepository).findByTaskId(taskId);
    }

    @Test
    void getAttachmentsByTaskId_TaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.getAttachmentsByTaskId(taskId));

        verify(taskRepository).findById(taskId);
        verify(attachmentRepository, never()).findByTaskId(any());
    }

    @Test
    void downloadAttachment_Success() throws IOException {
        Path testFilePath = tempDir.resolve("test.txt");
        Files.write(testFilePath, "test content".getBytes());

        attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .fileType("text/plain")
                .filePath(testFilePath.toString())
                .fileSize(100L)
                .task(task)
                .uploadDate(LocalDateTime.now())
                .build();
                
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        Resource resource = attachmentService.downloadAttachment(attachmentId);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals("test.txt", resource.getFilename());
        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void downloadAttachment_NotFound() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.downloadAttachment(attachmentId));

        verify(attachmentRepository).findById(attachmentId);
    }

    @Test
    void deleteAttachment_Success() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(attachmentId);

        verify(attachmentRepository).findById(attachmentId);
        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void deleteAttachment_NotFound() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> attachmentService.deleteAttachment(attachmentId));

        verify(attachmentRepository).findById(attachmentId);
        verify(attachmentRepository, never()).delete(any());
    }

    @Test
    void deleteAttachment_UnauthorizedAccess() {
        User differentUser = User.builder()
                .id(UUID.randomUUID())
                .role(UserRole.TEAM_MEMBER)
                .department("HR")
                .build();
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);
        when(authentication.getPrincipal()).thenReturn(differentUserDetails);
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        assertThrows(DeniedAccessException.class,
                () -> attachmentService.deleteAttachment(attachmentId));

        verify(attachmentRepository).findById(attachmentId);
        verify(attachmentRepository, never()).delete(any());
    }
}