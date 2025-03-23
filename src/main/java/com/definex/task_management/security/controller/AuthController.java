package com.definex.task_management.security.controller;

import com.definex.task_management.dto.JwtResponse;
import com.definex.task_management.dto.LoginRequest;
import com.definex.task_management.dto.UserRequest;
import com.definex.task_management.dto.UserResponse;
import com.definex.task_management.enums.UserRole;
import com.definex.task_management.exception.DeniedAccessException;
import com.definex.task_management.security.service.AuthenticationService;
import com.definex.task_management.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "1. Authentication")
public class AuthController {
    private final AuthenticationService authenticationService;
    private final UserService userService;

    public AuthController(AuthenticationService authenticationService, UserService userService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }

    @PostMapping("/register/init")
    public ResponseEntity<UserResponse> initializeFirstAdmin(@Valid @RequestBody UserRequest request) {
        if (userService.hasAnyUsers()) {
            throw new DeniedAccessException("System is already initialized with users");
        }

        request.setRole(UserRole.ADMIN);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUser(request));
    }
}
