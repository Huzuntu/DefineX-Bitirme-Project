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
- Apache Kafka
- Docker
- JWT Authentication
- Swagger UI

## Prerequisites

- Docker and Docker Compose
- Java 21 (for local development)
- Maven (for local development)

## Getting Started

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
   - Kafka UI (http://localhost:8080)
   - PostgreSQL
   - Redis
   - Kafka
   - Zookeeper

3. Access the application:
   - API Documentation: http://localhost:8086/swagger-ui.html
   - Kafka UI: http://localhost:8080

## Development Setup

1. Install dependencies:
   ```bash
   mvn clean install
   ```

2. Run tests:
   ```bash
   mvn test
   ```

3. Run the application locally:
   ```bash
   mvn spring-boot:run
   ```

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

## Environment Setup

This project uses environment variables for configuration. These variables are stored in a `.env` file in the root directory.

### Setting up the .env file

1. Create a `.env` file in the root directory (a template is provided in the repository)
2. Configure the following variables:

```
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/defineX-task
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Redis Configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# Kafka Configuration
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

## Running the Application

1. Make sure you have set up your `.env` file
2. Run the application with:

```
./mvnw spring-boot:run
```

For testing:

```
./mvnw test
``` 