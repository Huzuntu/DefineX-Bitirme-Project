package com.definex.task_management.enums;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum ProjectStatus {
    IN_PROGRESS, COMPLETED, CANCELLED;

    private static final Map<ProjectStatus, Set<ProjectStatus>> validProjectStatusTransitions;

    static {
        Map<ProjectStatus, Set<ProjectStatus>> map = new EnumMap<>(ProjectStatus.class);
        map.put(IN_PROGRESS, EnumSet.of(COMPLETED, CANCELLED));
        map.put(COMPLETED, EnumSet.noneOf(ProjectStatus.class));
        map.put(CANCELLED, EnumSet.noneOf(ProjectStatus.class));
        validProjectStatusTransitions = Collections.unmodifiableMap(map);
    }

    public boolean canTransitionTo(ProjectStatus target) {
        return validProjectStatusTransitions.get(this).contains(target);
    }
}

