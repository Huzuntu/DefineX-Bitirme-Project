package com.definex.task_management.controller;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.service.AttachmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/attachments")
@Tag(name = "6. Attachment Management")
@Slf4j
public class AttachmentController {
    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") UUID taskId) {
        log.info("Uploading attachment for task id: {}", taskId);
        AttachmentRequest request = new AttachmentRequest();
        request.setFile(file);
        request.setTaskId(taskId);
        return ResponseEntity.ok(attachmentService.uploadAttachment(request));
    }

    @GetMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<AttachmentResponse> getAttachmentById(@PathVariable UUID attachmentId) {
        log.info("Fetching attachment with id: {}", attachmentId);
        return ResponseEntity.ok(attachmentService.getAttachmentById(attachmentId));
    }

    @GetMapping("/task/{taskId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsByTaskId(@PathVariable UUID taskId) {
        log.info("Fetching all attachments for task id: {}", taskId);
        return ResponseEntity.ok(attachmentService.getAttachmentsByTaskId(taskId));
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID attachmentId) {
        log.info("Deleting attachment with id: {}", attachmentId);
        attachmentService.deleteAttachment(attachmentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("hasAnyRole('ROLE_PROJECT_GROUP_MANAGER', 'ROLE_PROJECT_MANAGER', 'ROLE_TEAM_LEADER', 'ROLE_TEAM_MEMBER')")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable UUID attachmentId) {
        log.info("Downloading attachment with id: {}", attachmentId);
        Resource resource = attachmentService.downloadAttachment(attachmentId);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
} 