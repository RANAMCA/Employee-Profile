# Employee Profile HR Application

A full-stack HR application for managing employee profiles, absence requests, and feedback with AI-enhanced features. Built with Spring Boot 3.x and a modern multi-database architecture.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Features](#features)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Project Structure](#project-structure)

## Overview

This application demonstrates enterprise-grade Java development with:
- Multi-database architecture (PostgreSQL + MongoDB + Redis)
- Role-based access control (RBAC) with JWT authentication
- AI-enhanced feedback polishing using HuggingFace
- Comprehensive integration testing with Testcontainers
- API-first design with OpenAPI 3.x
- Production-ready Docker deployment

## Technology Stack

### Core Framework
- **Spring Boot 3.2.0** - Main application framework
- **Java 21** - Latest LTS version with modern language features
- **Maven** - Build and dependency management

### Data Layer
- **PostgreSQL 15** - Relational data (employees, absences)
- **MongoDB 7.0** - Document storage (feedback)
- **Redis 7** - Caching layer for AI responses
- **Spring Data JPA** - ORM for PostgreSQL
- **Spring Data MongoDB** - MongoDB integration
- **Flyway** - Database migrations

### Security
- **Spring Security** - Authentication and authorization
- **JWT (JJWT 0.12.3)** - Token-based authentication
- **BCrypt** - Password hashing

### API & Documentation
- **SpringDoc OpenAPI 3** - API documentation and Swagger UI
- **Jackson** - JSON serialization with role-based views

### Code Quality
- **MapStruct 1.5.5** - Type-safe DTO mapping
- **Lombok** - Boilerplate reduction
- **Bean Validation** - Request validation

### AI Integration
- **WebClient (WebFlux)** - Reactive HTTP client
- **HuggingFace API** - AI feedback polishing
- **Redisson** - Advanced Redis client

### Testing
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing with real databases
- **MockMvc** - REST API testing
- **AssertJ** - Fluent assertions

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## Architecture

### Multi-Database Strategy

```
┌─────────────────┐
│   Spring Boot   │
│   Application   │
└────────┬────────┘
         │
    ┌────┴─────────────────┬──────────────┐
    │                      │              │
    ▼                      ▼              ▼
┌─────────┐          ┌──────────┐   ┌────────┐
│PostgreSQL│          │ MongoDB  │   │ Redis  │
│         │          │          │   │        │
│Employees│          │Feedbacks │   │AI Cache│
│Absences │          │          │   │        │
└─────────┘          └──────────┘   └────────┘
```

### Role-Based Access Control

| Role      | Permissions                                                   |
|-----------|--------------------------------------------------------------|
| MANAGER   | Full access: View/edit all employees, manage absences       |
| EMPLOYEE  | Own profile management, create absence requests, feedback   |
| COWORKER  | Limited view, leave feedback                                 |

### Key Design Patterns

- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Separation of domain and API models
- **Service Layer** - Business logic encapsulation
- **Builder Pattern** - Object construction (Lombok)
- **Strategy Pattern** - Role-based authorization
- **Factory Pattern** - UserPrincipal creation

## Features

### 1. Authentication & Authorization
- JWT-based authentication with refresh tokens
- Role-based access control
- Secure password hashing with BCrypt
- Token expiration handling

### 2. Employee Management
- CRUD operations with role-based restrictions
- Profile search and filtering
- Department-wise employee listing
- Soft delete (deactivation)

### 3. Absence Request Management
- Create and manage leave requests
- Manager approval workflow
- Conflict detection for overlapping requests
- Status tracking (PENDING, APPROVED, REJECTED, CANCELLED)

### 4. AI-Enhanced Feedback
- Feedback creation and management
- AI-powered feedback polishing via HuggingFace
- Redis caching for improved performance
- Visibility controls

### 5. API Documentation
- Interactive Swagger UI at `/swagger-ui.html`
- OpenAPI 3.0 specification at `/api-docs`
- Comprehensive endpoint documentation

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker and Docker Compose (for easy setup)
- (Optional) HuggingFace API key for AI features

### Quick Start with Docker Compose

1. **Clone the repository**
   ```bash
   git clone https://github.com/RANAMCA/Employee-Profile.git
   cd Employee-Profile
   ```

2. **Set up environment variables**
   ```bash
   cp .env.example .env
   # Edit .env and add your HuggingFace API key (optional)
   ```

3. **Start all services (first time - build required)**
   ```bash
   docker-compose up --build
   ```

   Or run in detached mode (background):
   ```bash
   docker-compose up --build -d
   ```

   **Note**: First build takes 5-10 minutes as Maven downloads dependencies.

   To view logs if running in detached mode:
   ```bash
   docker-compose logs -f app
   ```

4. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - API Docs: http://localhost:8080/api-docs

   Wait for the log message: `Started EmployeeProfileApplication` before accessing.

5. **Database Ports (Important)**
   - PostgreSQL: `localhost:5433` (port 5433 to avoid conflicts with local PostgreSQL)
   - MongoDB: `localhost:27017`
   - Redis: `localhost:6379`

   If you have local PostgreSQL running on port 5432, there's no conflict. Both can run simultaneously.

   To connect with pgAdmin or other GUI tools:
   - Host: `localhost`
   - Port: `5433`
   - Database: `employee_profile`
   - Username: `postgres`
   - Password: `postgres`

### Local Development Setup

1. **Start databases**
   ```bash
   docker-compose up postgres mongodb redis -d
   ```

2. **Configure application**
   ```bash
   cp .env.example .env
   # Update database URLs for localhost
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### Sample Data

The application automatically creates sample users on startup:

| Email                        | Password  | Role      |
|------------------------------|-----------|-----------|
| john.manager@newwork.com     | password  | MANAGER   |
| jane.smith@newwork.com       | password  | EMPLOYEE  |
| bob.coworker@newwork.com     | password  | COWORKER  |

## API Documentation

### Authentication Endpoints

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "EMPLOYEE",
  "department": "Engineering",
  "position": "Software Engineer"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "employeeId": 1,
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "role": "EMPLOYEE"
}
```

### Employee Endpoints

All employee endpoints require authentication. Include JWT token in header:
```
Authorization: Bearer <your_jwt_token>
```

#### Get All Employees (Manager only)
```http
GET /api/employees
```

#### Get Employee by ID
```http
GET /api/employees/{id}
```

#### Update Employee
```http
PUT /api/employees/{id}
Content-Type: application/json

{
  "bio": "Updated bio",
  "skills": "Java, Spring Boot, Docker"
}
```

#### Search Employees (Manager only)
```http
GET /api/employees/search?keyword=john
```

### Absence Endpoints

#### Create Absence Request
```http
POST /api/absences
Content-Type: application/json

{
  "type": "VACATION",
  "startDate": "2024-12-20",
  "endDate": "2024-12-31",
  "reason": "Year-end vacation"
}
```

#### Get My Absences
```http
GET /api/absences/my
```

#### Get Pending Absences (Manager only)
```http
GET /api/absences/pending
```

#### Review Absence (Manager only)
```http
PATCH /api/absences/{id}/review
Content-Type: application/json

{
  "status": "APPROVED",
  "reviewComment": "Approved for vacation"
}
```

### Feedback Endpoints

#### Create Feedback
```http
POST /api/feedbacks
Content-Type: application/json

{
  "employeeId": 2,
  "content": "Great team player and excellent problem solver",
  "rating": 5,
  "category": "Performance",
  "polishWithAI": true
}
```

#### Get Feedback for Employee
```http
GET /api/feedbacks/employee/{employeeId}
```

## Testing

### Run All Tests
```bash
mvn test
```

### Run Integration Tests Only
```bash
mvn test -Dtest=**/*IT
```

### Test Coverage
- Unit tests for services and utilities
- Integration tests with Testcontainers
- REST API tests with MockMvc
- Security tests for authorization

### Test Containers
The integration tests automatically start:
- PostgreSQL container
- MongoDB container
- Redis container

No manual database setup required!

## Project Structure

```
src/
├── main/
│   ├── java/com/newwork/employeeprofile/
│   │   ├── config/              # Configuration classes
│   │   │   ├── OpenAPIConfig.java
│   │   │   ├── RedisConfig.java
│   │   │   └── SecurityConfig.java
│   │   ├── controller/          # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── EmployeeController.java
│   │   │   ├── AbsenceController.java
│   │   │   └── FeedbackController.java
│   │   ├── domain/              # JPA entities & enums
│   │   │   ├── Employee.java
│   │   │   ├── Absence.java
│   │   │   ├── Feedback.java (MongoDB)
│   │   │   └── UserRole.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── request/
│   │   │   └── response/
│   │   ├── exception/           # Custom exceptions
│   │   ├── mapper/              # MapStruct mappers
│   │   ├── repository/          # Spring Data repositories
│   │   ├── security/            # JWT & Security
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   └── UserPrincipal.java
│   │   ├── service/             # Business logic
│   │   │   ├── AuthService.java
│   │   │   ├── EmployeeService.java
│   │   │   ├── AbsenceService.java
│   │   │   ├── FeedbackService.java
│   │   │   └── AIService.java
│   │   └── EmployeeProfileApplication.java
│   └── resources/
│       ├── db/migration/        # Flyway migrations
│       │   ├── V1__create_employees_table.sql
│       │   └── V2__create_absences_table.sql
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-test.yml
└── test/
    └── java/com/newwork/employeeprofile/
        └── integration/         # Integration tests
            ├── BaseIntegrationTest.java
            ├── AuthControllerIT.java
            └── EmployeeControllerIT.java
```

## Key Implementation Highlights

### 1. Multi-Database Configuration
- PostgreSQL for structured data (employees, absences)
- MongoDB for document-oriented data (feedback)
- Redis for caching AI responses
- All configured in a single Spring Boot application

### 2. JWT Security
- Stateless authentication
- Role-based method security with @PreAuthorize
- Custom UserPrincipal for authentication context
- Token refresh mechanism

### 3. Role-Based JSON Views
- Different response views based on user roles
- MANAGER sees full data
- EMPLOYEE sees own data + limited colleague info
- COWORKER sees minimal public info

### 4. AI Integration with Caching
- HuggingFace API integration for feedback polishing
- Redis caching to reduce API calls
- Fallback to original content on AI failure
- Configurable caching strategy

### 5. Database Migration
- Flyway for version-controlled schema changes
- Sample data seeded via migrations
- Repeatable migrations for data updates

### 6. Testing Strategy
- Testcontainers for real database testing
- Isolated test environments
- Complete API endpoint coverage
- Security testing included

## Environment Variables

| Variable                  | Description                           | Default                           |
|---------------------------|---------------------------------------|-----------------------------------|
| POSTGRES_URL              | PostgreSQL connection URL             | jdbc:postgresql://localhost:5432/employee_profile |
| MONGODB_URI               | MongoDB connection URI                | mongodb://localhost:27017/employee_profile |
| REDIS_HOST                | Redis server host                     | localhost                         |
| JWT_SECRET                | JWT signing secret (change in prod!)  | (see .env.example)                |
| HUGGINGFACE_API_KEY       | HuggingFace API key for AI            | (optional)                        |
| SPRING_PROFILE            | Active Spring profile                 | dev                               |

## Production Deployment

1. **Update JWT secret**
   - Generate a secure random key for production
   - Update JWT_SECRET environment variable

2. **Configure databases**
   - Use managed database services
   - Enable SSL/TLS connections
   - Set up database backups

3. **Enable HTTPS**
   - Configure SSL certificates
   - Use reverse proxy (nginx/traefik)

4. **Monitoring**
   - Spring Boot Actuator endpoints enabled
   - Health checks configured
   - Prometheus metrics available at `/actuator/prometheus`

## Future Enhancements

- [ ] Kafka integration for event-driven architecture
- [ ] Real-time notifications with WebSockets
- [ ] Advanced analytics dashboard
- [ ] File upload for employee documents
- [ ] Email notifications for absence approvals
- [ ] Performance metrics and monitoring
- [ ] GraphQL API support

## License

This project is created as a take-home assignment for NEWWORK.

## Contact

For questions or feedback, please reach out to the development team.

---

**Built with passion for demonstrating enterprise Java development skills**
