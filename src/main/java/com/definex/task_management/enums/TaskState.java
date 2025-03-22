package com.definex.task_management.enums;

import java.util.*;

public enum TaskState {
    BACKLOG, IN_ANALYSIS, IN_DEVELOPMENT, BLOCKED, CANCELLED, COMPLETED;

    private static final Map<TaskState, Set<TaskState>> validTransitions;

    static {
        Map<TaskState, Set<TaskState>> map = new EnumMap<>(TaskState.class);
        map.put(BACKLOG, EnumSet.of(IN_ANALYSIS, CANCELLED));
        map.put(IN_ANALYSIS, EnumSet.of(BACKLOG, IN_DEVELOPMENT, BLOCKED, CANCELLED));
        map.put(IN_DEVELOPMENT, EnumSet.of(IN_ANALYSIS, COMPLETED, BLOCKED, CANCELLED));
        map.put(BLOCKED, EnumSet.of(IN_ANALYSIS, IN_DEVELOPMENT, CANCELLED));
        map.put(CANCELLED, EnumSet.noneOf(TaskState.class));
        map.put(COMPLETED, EnumSet.noneOf(TaskState.class));
        validTransitions = Collections.unmodifiableMap(map);
    }

    public boolean canTransitionTo(TaskState target, String reason) {
        if (this == COMPLETED || this == CANCELLED) {
            return false;
        }

        if ((target == CANCELLED || target == BLOCKED) && (reason == null || reason.isBlank())) {
            return false;
        }

        return validTransitions.get(this).contains(target);
    }
}
