package com.definex.task_management.dto;

import com.definex.task_management.enums.TaskState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStateUpdateRequest {
    private TaskState newState;
    private String reason;
} 