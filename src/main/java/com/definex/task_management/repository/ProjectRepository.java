package com.definex.task_management.repository;

import com.definex.task_management.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    List<Project> findByDepartment(String department);

    @Query("SELECT p FROM Project p JOIN p.teamMembers m WHERE p.department = :department AND m.id = :userId")
    List<Project> findByDepartmentAndTeamMembersContaining(@Param("department") String department, @Param("userId") UUID userId);
}
