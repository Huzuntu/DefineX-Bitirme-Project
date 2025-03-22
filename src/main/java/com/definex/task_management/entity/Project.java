package com.definex.task_management.entity;


import com.definex.task_management.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"teamMembers", "tasks"})
@SQLDelete(sql = "UPDATE projects SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Project extends BaseEntity {

    @NotBlank
    private String title;

    private String description;

    @NotBlank
    private String department;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    @ManyToMany
    @Builder.Default
    @JoinTable(
            name = "project_team_members",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> teamMembers = new HashSet<>();

    @OneToMany(mappedBy = "project")
    @Builder.Default
    private Set<Task> tasks = new HashSet<>();
}