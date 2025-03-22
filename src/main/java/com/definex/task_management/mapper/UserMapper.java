package com.definex.task_management.mapper;

import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public static UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .role(user.getRole())
                .build();
    }

    public static User toEntity(UserRequest userRequest) {
        return User.builder()
                .name(userRequest.getName())
                .email(userRequest.getEmail())
                .department(userRequest.getDepartment())
                .role(userRequest.getRole())
                .build();
    }
} 