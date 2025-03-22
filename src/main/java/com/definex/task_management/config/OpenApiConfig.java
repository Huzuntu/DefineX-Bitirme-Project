package com.definex.task_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Arrays;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI defineXTaskManagementOpenAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .name("Bearer Authentication")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .info(new Info()
                        .title("DefineX Task Management API")
                        .description("""
                                Advanced Task Management System API Documentation
                                
                                This API provides comprehensive endpoints for managing projects, tasks, and team members:
                                
                                ## Key Features
                                * **Authentication & Authorization**
                                * **Project Management**
                                * **Task Management with State Transitions**
                                * **Team Member Assignment**
                                * **File Attachments**
                                * **Comment Management**
                                
                                ## Task State Transitions
                                * Happy Path: `Backlog ⇔ In Analysis ⇔ In Development/Progress ⇔ Completed`
                                * Cancel Path: Any state (except Completed) can transition to Cancelled
                                * Blocked Paths: `In Analysis ⇔ Blocked`, `In Development/Progress ⇔ Blocked`
                                
                                ## Role-Based Access
                                * Project Group Manager: Full access
                                * Project Manager: Project-level management
                                * Team Leader: Task management and team coordination
                                * Team Member: Limited task updates and comments""")
                        .version("1.0")
                        .contact(new Contact()
                                .name("DefineX")
                                .email("contact@definex.com"))
                        .license(new License()
                                .name("Private License")
                                .url("https://definex.com")))
                .addServersItem(new Server().url("http://localhost:8086").description("Local Development Server"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .tags(Arrays.asList(
                        new Tag().name("1. Authentication").description("User authentication and registration endpoints"),
                        new Tag().name("2. User Management").description("User CRUD operations and role management"),
                        new Tag().name("3. Project Management").description("Project creation, updates, and team assignment"),
                        new Tag().name("4. Task Management").description("Task operations including state and priority management"),
                        new Tag().name("5. Comment Management").description("Task comment operations"),
                        new Tag().name("6. Attachment Management").description("File attachment operations for tasks")
                ));
    }
} 