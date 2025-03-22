package com.definex.task_management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentRequest {
    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotNull(message = "Task ID is required")
    private UUID taskId;
} 