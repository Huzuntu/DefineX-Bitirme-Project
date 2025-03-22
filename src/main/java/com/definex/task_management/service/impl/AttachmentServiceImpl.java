package com.definex.task_management.service.impl;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.entity.Attachment;
import com.definex.task_management.entity.Task;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.AttachmentMapper;
import com.definex.task_management.repository.AttachmentRepository;
import com.definex.task_management.repository.TaskRepository;
import com.definex.task_management.security.CustomUserDetails;
import com.definex.task_management.service.AttachmentService;
import com.definex.task_management.service.BaseService;
import com.definex.task_management.service.TaskService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttachmentServiceImpl extends BaseService implements AttachmentService {
    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final Path fileStorageLocation;

    public AttachmentServiceImpl(
            AttachmentRepository attachmentRepository,
            TaskRepository taskRepository,
            @Value("${app.file-storage-location}") String fileStorageLocation
    ) {
        this.attachmentRepository = attachmentRepository;
        this.taskRepository = taskRepository;
        this.fileStorageLocation = Paths.get(fileStorageLocation).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public AttachmentResponse uploadAttachment(AttachmentRequest attachmentRequest) {
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(attachmentRequest.getTaskId());

        validateUserAccessToProject(currentUser, task.getProject());
        validateUserAccessToTask(currentUser, task);

        try {
            MultipartFile file = attachmentRequest.getFile();
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);

            Attachment attachment = AttachmentMapper.toEntity(attachmentRequest, task, targetLocation.toString());
            Attachment savedAttachment = attachmentRepository.save(attachment);
            return AttachmentMapper.toResponse(savedAttachment);
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public AttachmentResponse getAttachmentById(UUID attachmentId) {
        CustomUserDetails currentUser = getCurrentUser();
        Attachment attachment = getAttachmentEntityById(attachmentId);

        validateUserAccessToProject(currentUser, attachment.getTask().getProject());
        validateUserAccessToTask(currentUser, attachment.getTask());

        return AttachmentMapper.toResponse(attachment);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public List<AttachmentResponse> getAttachmentsByTaskId(UUID taskId) {
        CustomUserDetails currentUser = getCurrentUser();
        Task task = getTaskEntityById(taskId);

        validateUserAccessToProject(currentUser, task.getProject());
        validateUserAccessToTask(currentUser, task);
        List<Attachment> attachments = attachmentRepository.findByTaskId(taskId);
        return attachments.stream()
                .map(AttachmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public Resource downloadAttachment(UUID attachmentId) {
        CustomUserDetails currentUser = getCurrentUser();
        Attachment attachment = getAttachmentEntityById(attachmentId);

        validateUserAccessToProject(currentUser, attachment.getTask().getProject());
        validateUserAccessToTask(currentUser, attachment.getTask());

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new EntityNotFoundException("File not found: " + attachment.getFileName());
            }
        } catch (MalformedURLException ex) {
            throw new EntityNotFoundException("File not found: " + attachment.getFileName(), ex);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public void deleteAttachment(UUID attachmentId) {
        CustomUserDetails currentUser = getCurrentUser();
        Attachment attachment = getAttachmentEntityById(attachmentId);

        validateUserAccessToProject(currentUser, attachment.getTask().getProject());
        validateUserAccessToTask(currentUser, attachment.getTask());

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
            attachmentRepository.delete(attachment);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file. Please try again!", ex);
        }
    }

    private Attachment getAttachmentEntityById(UUID attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new EntityNotFoundException("Attachment not found with id: " + attachmentId));
    }

    private Task getTaskEntityById(UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
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