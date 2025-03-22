# DefineX Advanced Task Management System

This project is an advanced task management system developed for DefineX, providing comprehensive project and task management capabilities with modern features and a robust architecture.

## Features

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

- **User Management**
  - Role-based access control
  - Team member assignment
  - Authentication and authorization

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

   All services are pre-configured to work together.

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

   Note: `host.docker.internal` is used to connect to services running on your host machine from within the Docker container.

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

## API Documentation

The API documentation is available through Swagger UI at http://localhost:8086/swagger-ui.html

Key endpoints include:
- `/api/auth` - Authentication endpoints
- `/api/projects` - Project management
- `/api/tasks` - Task management
- `/api/users` - User management

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