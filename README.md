# DefineX Advanced Task Management System

This project is an advanced task management system developed for DefineX Spring Boot Bootcamp Graduation Project. Project and task management capabilities with modern features and a monolithic architecture.

## Features

- **User Management**
    - Role-based access control
    - Team member assignment
    - Authentication and authorization

- **Project Management**
  - Create and manage projects
  - Associate projects with departments
  - Track project status
  - Manage team members

- **Task Management**
  - Create and assign tasks
  - Track task progress with state transitions
  - Set task priorities
  - Attach files to tasks
  - Add comments to tasks



## Technology Stack

- Java 21
- Spring Boot 3.4.3
- PostgreSQL
- Redis
- Docker
- JWT Authentication
- Swagger UI

## Prerequisites

- Docker and Docker Compose (for containerized setup)
- Java 21 (for local development)
- Maven (for local development)
- PostgreSQL (for local development)
- Redis (for local development)

## Getting Started

### Option 1: Running with Docker Compose (Recommended)

The easiest way to run the application is using Docker Compose, which will set up PostgreSQL, Redis, and the application in a single command.

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/task-management.git
   cd task-management
   ```

2. Run with Docker Compose:
   ```bash
   docker-compose up -d
   ```

   This will start:
   - Task Management Application (http://localhost:8086)
   - PostgreSQL database
   - Redis cache

   All services are configured to work together.

3. Access the application:
   - API Documentation: http://localhost:8086/swagger-ui.html

4. To stop all services:
   ```bash
   docker-compose down
   ```

5. To stop and remove volumes (data will be lost):
   ```bash
   docker-compose down -v
   ```

### Option 2: Running Single Docker Container

If you already have PostgreSQL and Redis running, you can just run the application container:

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/task-management.git
   cd task-management
   ```

2. Build and run the Docker container:
   ```bash
   docker build -t task-management .
   docker run -p 8086:8086 \
     -e DB_URL=jdbc:postgresql://host.docker.internal:5432/defineX-task \
     -e DB_USERNAME=postgres \
     -e DB_PASSWORD=password \
     -e REDIS_HOST=host.docker.internal \
     task-management
   ```

### Option 3: Running Locally

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/task-management.git
   cd task-management
   ```

2. Set up your PostgreSQL database:
   - Create a database named `defineX-task`
   - Use default username `postgres` or update in your .env file

3. Configure environment variables:
   - Create a `.env` file in the root directory with the following content:
   ```
   DB_URL=jdbc:postgresql://localhost:5432/defineX-task
   DB_USERNAME=postgres
   DB_PASSWORD=your_password

   JWT_SECRET=your_jwt_secret_key
   JWT_EXPIRATION=86400000

   REDIS_HOST=localhost
   REDIS_PORT=6379
   REDIS_PASSWORD=

   FILE_STORAGE_LOCATION=./uploads

   CORS_ALLOWED_ORIGINS=*
   CORS_ALLOWED_METHODS=GET,POST,PUT,PATCH,DELETE,OPTIONS
   CORS_ALLOWED_HEADERS=authorization,content-type,x-auth-token
   CORS_EXPOSED_HEADERS=x-auth-token
   CORS_MAX_AGE=3600
   ```

4. Install dependencies and run the application:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Access the application:
   - API Documentation: http://localhost:8086/swagger-ui.html

## Testing the API with Swagger UI

Once the application is running, you can access the Swagger UI at http://localhost:8086/swagger-ui.html to test all API endpoints.

### API Categories

The API endpoints are organized into the following categories for easier navigation:

1. **Authentication** - User login, registration, and token management
2. **User Management** - User profile and role operations
3. **Project Management** - Project creation and team assignments
4. **Task Management** - Task operations and state transitions
5. **Comment System** - Task comments and mentions
6. **Attachment Management** - File uploads and downloads

### Authentication Steps

1. **Register and Login**: 
   - Admin UserRole is only for creating and deleting Users 
   - Use the `/api/auth/register/init` endpoint to create a new admin user
   - Use the `/api/auth/login` endpoint with your credentials
   - Copy the JWT token from the response

2. **Authorize in Swagger UI**:
   - Click the "Authorize" button at the top right of the Swagger UI
   - In the input field, enter: `Bearer your_token_here` (replace with your actual token you got from login response)
   - Click "Authorize" then "Close"
   - All requests will now include your authentication token

### Testing Core Features

#### Project Management
1. Create a new project using POST `/api/projects`
2. View all projects with GET `/api/projects`
3. Assign team members to a project using POST `/api/projects/{projectId}/members`

#### Task Management
1. Prerequisite: Project must be created before creating a task
2. Create tasks within a project using POST `/api/tasks`
3. Update task status with PUT `/api/tasks/{taskId}`
4. View tasks by various filters using GET `/api/tasks`

#### Task State Transitions
1. Tasks follow a specific workflow:
   - Backlog -> In Analysis -> In Development -> Completed
2. Test state transitions using the task update endpoint
3. Try invalid transitions to see validation errors (Example: In Analysis -> Completed is not valid)

#### File Attachments
1. Attach files to tasks using POST `/api/attachments`
2. View task attachments with GET `/api/tasks/{taskId}/attachments`
3. Download attachments using GET `/api/attachments/{attachmentId}`

#### Comments
1. Add comments to tasks using POST `/api/comments`
2. View task comments with GET `/api/tasks/{taskId}/comments`

### Testing Different User Roles

1. Create users with different roles:
   - Project Group Manager
   - Project Manager
   - Team Leader
   - Team Member

2. Test the permission boundaries by:
   - Logging in as different user roles (and authenticate yourself with login response)
   - Some of the operations are not authorized for some user roles
   - Observing the appropriate permission errors

## API Documentation

The API documentation is available through Swagger UI at http://localhost:8086/swagger-ui.html

### Endpoints

#### 1. Authentication
- `POST /api/v1/auth/login` - Login with username and password
- `POST /api/v1/auth/register` - Register a new user
- `POST /api/v1/auth/register/init` - Initialize the first admin user (only works when no users exist)

#### 2. User Management
- `POST /api/v1/users` - Create a new user
- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/{userId}` - Get user by ID
- `PUT /api/v1/users/{userId}` - Update user
- `DELETE /api/v1/users/{userId}` - Delete user

#### 3. Project Management
- `POST /api/v1/projects` - Create a new project (ROLE_PROJECT_GROUP_MANAGER)
- `GET /api/v1/projects/department/{department}` - Get projects by department
- `GET /api/v1/projects/{projectId}` - Get project by ID
- `PUT /api/v1/projects/{projectId}` - Update project (ROLE_PROJECT_GROUP_MANAGER)
- `PATCH /api/v1/projects/{projectId}/status/{status}` - Update project status (ROLE_PROJECT_GROUP_MANAGER)
- `POST /api/v1/projects/{projectId}/tasks/{taskId}` - Assign task to project (ROLE_PROJECT_GROUP_MANAGER, ROLE_PROJECT_MANAGER)
- `POST /api/v1/projects/{projectId}/members/{userId}` - Add team member to project (ROLE_PROJECT_GROUP_MANAGER)
- `DELETE /api/v1/projects/{projectId}/members/{userId}` - Remove team member from project (ROLE_PROJECT_GROUP_MANAGER)
- `DELETE /api/v1/projects/{projectId}` - Delete project (ROLE_PROJECT_GROUP_MANAGER)

#### 4. Task Management
- `POST /api/v1/tasks` - Create a new task
- `GET /api/v1/tasks/{taskId}` - Get task by ID
- `GET /api/v1/tasks/project/{projectId}` - Get all tasks under a project
- `PUT /api/v1/tasks/{taskId}` - Update task
- `DELETE /api/v1/tasks/{taskId}` - Delete task
- `PATCH /api/v1/tasks/{taskId}/state` - Update task state
- `PATCH /api/v1/tasks/{taskId}/priority` - Update task priority
- `POST /api/v1/tasks/{taskId}/assignees/{userId}` - Assign user to task
- `DELETE /api/v1/tasks/{taskId}/assignees/{userId}` - Remove user from task

#### 5. Comment System
- `POST /api/v1/comments` - Create a new comment
- `GET /api/v1/comments/{commentId}` - Get comment by ID
- `GET /api/v1/comments/task/{taskId}` - Get all comments for a task
- `PUT /api/v1/comments/{commentId}` - Update comment
- `DELETE /api/v1/comments/{commentId}` - Delete comment

#### 6. Attachment Management
- `POST /api/v1/attachments` - Upload attachment
- `GET /api/v1/attachments/{attachmentId}` - Get attachment by ID
- `GET /api/v1/attachments/task/{taskId}` - Get all attachments for a task
- `DELETE /api/v1/attachments/{attachmentId}` - Delete attachment
- `GET /api/v1/attachments/{attachmentId}/download` - Download attachment

### Authorization

Each endpoint has specific role-based access controls. The following roles are available:
- ADMIN (Only related to User creation and deletion)
- PROJECT_GROUP_MANAGER (Can do everything related to his/her department)
- PROJECT_MANAGER 
- TEAM_LEADER
- TEAM_MEMBER

## Task State Transitions

Tasks follow a specific state transition flow:
- Happy Path: Backlog ⇔ In Analysis ⇔ In Development/Progress ⇔ Completed
- Cancel Path: Any state (except Completed) can transition to Cancelled
- Blocked Paths: In Analysis ⇔ Blocked, In Development/Progress ⇔ Blocked

## Security

- JWT-based authentication
- Role-based access control
- Secure file storage
- API endpoint protection

## Development

### Running Tests

```bash
mvn test
```

### Building the Application

```bash
mvn clean package
```

## Project Structure

- `/src/main/java` - Java source code
- `/src/main/resources` - Configuration files
- `/src/test` - Test classes
- `/uploads` - File storage location
- `docker-compose.yml` - Docker Compose configuration
- `Dockerfile` - Docker container configuration

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is proprietary and confidential. Unauthorized copying or distribution of this project's files, via any medium, is strictly prohibited.

## Contact

DefineX - contact@definex.com 