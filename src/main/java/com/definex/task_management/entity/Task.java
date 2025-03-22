package com.definex.task_management.entity;


import com.definex.task_management.enums.TaskPriority;
import com.definex.task_management.enums.TaskState;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"assignees", "comments", "attachments"})
@SQLDelete(sql = "UPDATE tasks SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class Task extends BaseEntity {
    private String title;
    private String userStory;
    private String acceptanceCriteria;

    @Enumerated(EnumType.STRING)
    private TaskState state;

    @Column(columnDefinition = "TEXT")
    private String stateTransitionReason;

    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToMany
    @Builder.Default
    @JoinTable(
            name = "task_assignees",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> assignees = new HashSet<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Attachment> attachments = new ArrayList<>();
}