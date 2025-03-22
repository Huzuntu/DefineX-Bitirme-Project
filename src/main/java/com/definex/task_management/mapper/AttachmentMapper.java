package com.definex.task_management.mapper;

import com.definex.task_management.dto.AttachmentRequest;
import com.definex.task_management.dto.AttachmentResponse;
import com.definex.task_management.entity.Attachment;
import com.definex.task_management.entity.Task;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AttachmentMapper {
    public static Attachment toEntity(AttachmentRequest request, Task task, String filePath) {
        MultipartFile file = request.getFile();
        return Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(filePath)
                .task(task)
                .build();
    }

    public static AttachmentResponse toResponse(Attachment attachment) {
        return AttachmentResponse.builder()
                .id(attachment.getId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .taskId(attachment.getTask().getId())
                .uploadDate(attachment.getUploadDate())
                .build();
    }
}