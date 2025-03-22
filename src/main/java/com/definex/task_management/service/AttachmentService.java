package com.definex.task_management.service;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.UUID;

public interface AttachmentService {
    AttachmentResponse uploadAttachment(AttachmentRequest attachmentRequest);
    AttachmentResponse getAttachmentById(UUID attachmentId);
    List<AttachmentResponse> getAttachmentsByTaskId(UUID taskId);
    Resource downloadAttachment(UUID attachmentId);
    void deleteAttachment(UUID attachmentId);
} 