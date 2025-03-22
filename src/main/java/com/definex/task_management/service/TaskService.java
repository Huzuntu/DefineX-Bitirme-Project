package com.definex.task_management.service;

import com.definex.task_management.dto.TaskRequest;
import com.definex.task_management.dto.TaskResponse;
import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;

import java.util.List;
import java.util.UUID;

public interface TaskService {
    public TaskResponse createTask(TaskRequest taskRequest);
    public TaskResponse getTaskById(UUID taskId);
    public List<TaskResponse> getAllTasksUnderProject(UUID projectId);
    public TaskResponse updateTask(UUID taskId, TaskRequest taskRequest);
    public TaskResponse deleteTask(UUID taskId);
    public TaskResponse updateTaskState(UUID taskId, TaskState taskState, String reason);
    public TaskResponse updateTaskPriority(UUID taskId, TaskPriority taskPriority);
    public TaskResponse assignUserToTask(UUID taskId, UUID userId);
    public TaskResponse removeUserFromTask(UUID taskId, UUID userId);

    
}
