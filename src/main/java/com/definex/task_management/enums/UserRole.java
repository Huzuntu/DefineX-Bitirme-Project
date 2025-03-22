package com.definex.task_management.enums;

import java.util.EnumSet;
import java.util.Set;

public enum UserRole {
    ADMIN(EnumSet.allOf(Permission.class)),
    PROJECT_GROUP_MANAGER(EnumSet.allOf(Permission.class)),
    PROJECT_MANAGER(createProjectManagerPermissions()),

    TEAM_LEADER(EnumSet.of(
            Permission.PROJECT_READ,
            Permission.TASK_CREATE, Permission.TASK_READ, Permission.TASK_UPDATE,
            Permission.TASK_ASSIGN, Permission.TASK_CHANGE_STATE, Permission.TASK_CHANGE_PRIORITY,
            Permission.COMMENT_CREATE, Permission.COMMENT_READ, Permission.COMMENT_UPDATE, Permission.COMMENT_DELETE,
            Permission.ATTACHMENT_CREATE, Permission.ATTACHMENT_READ, Permission.ATTACHMENT_UPDATE, Permission.ATTACHMENT_DELETE
    )),

    TEAM_MEMBER(EnumSet.of(
            Permission.PROJECT_READ,
            Permission.TASK_READ,
            Permission.TASK_CHANGE_STATE,
            Permission.COMMENT_CREATE, Permission.COMMENT_READ, Permission.COMMENT_UPDATE, Permission.COMMENT_DELETE,
            Permission.ATTACHMENT_CREATE, Permission.ATTACHMENT_READ, Permission.ATTACHMENT_DELETE, Permission.ATTACHMENT_UPDATE
    ));

    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    private static Set<Permission> createProjectManagerPermissions() {
        Set<Permission> permissions = EnumSet.allOf(Permission.class);
        permissions.remove(Permission.PROJECT_CREATE);
        permissions.remove(Permission.PROJECT_DELETE);
        return permissions;
    }
}