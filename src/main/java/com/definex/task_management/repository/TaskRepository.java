package com.definex.task_management.repository;

import com.definex.task_management.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByProjectId(UUID projectId);

    @Query(value = "UPDATE tasks SET project_id = :projectId WHERE id = :taskId", nativeQuery = true)
    int assignTaskToProject(@Param("taskId") UUID taskId, @Param("projectId") UUID projectId);
}
