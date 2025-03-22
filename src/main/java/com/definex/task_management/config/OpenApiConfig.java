package com.definex.task_management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
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
                                ##  Testing with Swagger UI
                                
                                ### Authentication Steps
                                1. First, register a new user or login with existing credentials at the `/api/auth` endpoints
                                2. Copy the JWT token from the response
                                3. Click the "Authorize" button at the top of the page
                                4. Paste your token with format: `Bearer your_token_here`
                                5. Click "Authorize" then "Close"
                                
                                ### Using the API
                                1. Endpoints are categorized by tags for easy navigation
                                2. Click on an endpoint to expand it
                                3. Click "Try it out" to test the endpoint
                                4. Fill in the required parameters
                                5. Click "Execute" to send the request
                                6. View the response below
                                
                                ### File Upload Testing
                                1. For file uploads, use the `/api/attachments` endpoint
                                2. Select the file using the file picker in the request body
                                3. Provide the required task ID
                                4. Execute the request
                                
                                ### Testing State Transitions
                                1. Use the task update endpoint to change task states
                                2. Only valid state transitions will be accepted
                                3. Error responses will indicate invalid transitions
                                """)
                        .version("1.0")
                        .contact(new Contact()
                                .name("DefineX")
                                .email("contact@definex.com")))
                .addServersItem(new Server().url("http://localhost:8086").description("Local Development Server"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme))
                .tags(Arrays.asList(
                        new Tag().name("1. Authentication").description("User registration, login, token management"),
                        new Tag().name("2. User Management").description("User profile, role management, permissions"),
                        new Tag().name("3. Project Management").description("Project CRUD, team assignment, tracking"),
                        new Tag().name("4. Task Management").description("Task operations, state transitions, priorities"),
                        new Tag().name("5. Comment System").description("Task comments, mentions, notifications"),
                        new Tag().name("6. Attachment Management").description("File upload, download, management for tasks")
                ));
    }
} 