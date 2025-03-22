package com.definex.task_management.entity;


import com.definex.task_management.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"projects", "assignedTasks"} )
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public class User extends BaseEntity {
    @NotBlank
    private String name;

    @Column(unique = true)
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    private String department;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @ManyToMany(mappedBy = "teamMembers")
    @Builder.Default
    private Set<Project> projects = new HashSet<>();

    @ManyToMany(mappedBy = "assignees")
    @Builder.Default
    private Set<Task> assignedTasks = new HashSet<>();
}


