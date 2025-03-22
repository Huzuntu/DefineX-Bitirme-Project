package com.definex.task_management.service;

import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    public UserResponse createUser(UserRequest userRequest);
    public UserResponse getUserById(UUID userId);
    public User getUserEntityById(UUID userId);
    public List<UserResponse> getAllUsers();
    public UserResponse updateUser(UUID userId, UserRequest userRequest);
    public void deleteUser(UUID userId);
    public boolean hasAnyUsers();
}
