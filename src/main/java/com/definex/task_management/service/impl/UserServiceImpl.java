package com.definex.task_management.service.impl;

import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.entity.User;
import com.definex.task_management.exception.EntityNotFoundException;
import com.definex.task_management.mapper.UserMapper;
import com.definex.task_management.repository.UserRepository;
import com.definex.task_management.service.UserService;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    @CacheEvict(value = "userCache", allEntries = true)
    public UserResponse createUser(UserRequest userRequest) {
        User newUser = UserMapper.toEntity(userRequest);
        String newPassword = passwordEncoder.encode(userRequest.getPassword());
        newUser.setPassword(newPassword);
        User savedUser = userRepository.save(newUser);
        return UserMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "#userId")
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return UserMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "#userId")
    public User getUserEntityById(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return user;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userCache", key = "'all'")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "userCache", allEntries = true)
    @PreAuthorize("hasRole('PROJECT_GROUP_MANAGER')")
    public UserResponse updateUser(UUID userId, UserRequest userRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }
        user.setDepartment(userRequest.getDepartment());
        user.setRole(userRequest.getRole());

        User updatedUser = userRepository.save(user);
        return UserMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userCache", allEntries = true)
    @PreAuthorize("hasRole('PROJECT_GROUP_MANAGER')")
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAnyUsers() {
        return userRepository.count() > 0;
    }
}
