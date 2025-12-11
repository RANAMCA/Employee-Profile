# Architecture Documentation

## System Overview

The Employee Profile HR Application is built using a modern, scalable microservices-ready architecture with clear separation of concerns and enterprise-grade patterns.

## Architecture Layers

### 1. Presentation Layer (Controllers)
- **Purpose**: Handle HTTP requests, validate input, return responses
- **Technologies**: Spring Web MVC, OpenAPI annotations
- **Key Classes**:
  - `AuthController` - Authentication endpoints
  - `EmployeeController` - Employee management
  - `AbsenceController` - Absence request management
  - `FeedbackController` - Feedback operations

### 2. Business Logic Layer (Services)
- **Purpose**: Implement business rules, orchestrate operations
- **Technologies**: Spring Services, transaction management
- **Key Classes**:
  - `AuthService` - Authentication logic
  - `EmployeeService` - Employee business logic with role-based access
  - `AbsenceService` - Absence workflow management
  - `FeedbackService` - Feedback management with AI integration
  - `AIService` - AI-powered text enhancement

### 3. Data Access Layer (Repositories)
- **Purpose**: Abstract database operations
- **Technologies**: Spring Data JPA, Spring Data MongoDB
- **Key Interfaces**:
  - `EmployeeRepository` - JPA repository for employees
  - `AbsenceRepository` - JPA repository for absences
  - `FeedbackRepository` - MongoDB repository for feedback

### 4. Security Layer
- **Purpose**: Authentication, authorization, JWT management
- **Technologies**: Spring Security, JJWT
- **Key Components**:
  - `SecurityConfig` - Security configuration
  - `JwtTokenProvider` - JWT generation and validation
  - `JwtAuthenticationFilter` - Request authentication
  - `UserPrincipal` - Authentication principal

### 5. Data Transfer Layer (DTOs)
- **Purpose**: Decouple API contracts from domain models
- **Technologies**: MapStruct, Jackson, Bean Validation
- **Key Components**:
  - Request DTOs - Input validation
  - Response DTOs - Role-based views
  - MapStruct mappers - Type-safe conversions

## Design Patterns

### 1. Repository Pattern
- Abstracts data access logic
- Provides clean separation between business and data layers
- Enables easy testing with mock repositories

### 2. DTO Pattern
- Separates internal domain models from external API contracts
- Allows different views for different roles
- Prevents over-fetching and under-fetching of data

### 3. Service Layer Pattern
- Encapsulates business logic
- Provides transaction boundaries
- Centralizes business rules

### 4. Builder Pattern
- Used extensively via Lombok @Builder
- Provides fluent, readable object construction
- Ensures immutability where needed

### 5. Strategy Pattern
- Role-based authorization strategies
- Different data views based on user roles
- Flexible business rule application

### 6. Factory Pattern
- UserPrincipal creation from Employee entities
- Centralizes object creation logic

### 7. Facade Pattern
- Controllers act as facades to services
- Simplifies complex subsystem interactions

## Multi-Database Strategy

### Why Multiple Databases?

1. **PostgreSQL** - Relational data requiring ACID transactions
   - Employee profiles with complex relationships
   - Absence requests with referential integrity
   - Structured data with foreign keys

2. **MongoDB** - Document-oriented flexible schemas
   - Feedback documents with varying structures
   - Easy scaling for high-volume feedback
   - Schema flexibility for future enhancements

3. **Redis** - High-performance caching
   - AI response caching
   - Session management (future)
   - Rate limiting (future)

### Database Transaction Management

- Each database has its own transaction manager
- Services use appropriate @Transactional annotations
- Cross-database transactions handled at service layer

## Security Architecture

### JWT Authentication Flow

```
1. User sends credentials → AuthController
2. AuthController → AuthService validates
3. AuthService → JwtTokenProvider generates JWT
4. JWT returned to user
5. User includes JWT in Authorization header
6. JwtAuthenticationFilter validates JWT
7. Sets SecurityContext with UserPrincipal
8. Controllers access via @AuthenticationPrincipal
```

### Role-Based Authorization

- Method-level security with @PreAuthorize
- Service-layer authorization checks
- Different JSON views based on roles
- Fine-grained access control

### Security Best Practices

- Passwords hashed with BCrypt (cost factor 10)
- JWT signed with HS512 algorithm
- Stateless authentication
- CORS configured properly
- SQL injection prevention via JPA
- XSS protection via proper encoding

## AI Integration Architecture

### Feedback Polishing Flow

```
1. User submits feedback → FeedbackController
2. FeedbackService checks polishWithAI flag
3. If true, calls AIService.polishFeedback()
4. AIService checks Redis cache
5. If cache miss, calls HuggingFace API
6. Response cached in Redis
7. Polished content saved to MongoDB
```

### Caching Strategy

- **Cache Key**: Original feedback content hash
- **TTL**: 1 hour (configurable)
- **Eviction**: LRU (Least Recently Used)
- **Cache Warming**: On-demand
- **Fallback**: Original content if AI fails

## Error Handling

### Global Exception Handler

- Catches all exceptions centrally
- Returns consistent error responses
- Logs errors appropriately
- HTTP status codes mapped correctly

### Exception Types

- `ResourceNotFoundException` → 404
- `BadRequestException` → 400
- `UnauthorizedException` → 401
- `AccessDeniedException` → 403
- Validation errors → 400 with field details

## Testing Architecture

### Test Pyramid

1. **Unit Tests** (planned)
   - Service layer logic
   - Utility methods
   - MapStruct mappers

2. **Integration Tests** (implemented)
   - Full application context
   - Real databases via Testcontainers
   - Complete request/response cycles

3. **E2E Tests** (future)
   - Browser-based testing
   - Complete user workflows

### Testcontainers Strategy

- PostgreSQL container for JPA tests
- MongoDB container for document tests
- Redis container for cache tests
- Isolated test environments
- Parallel test execution

## Scalability Considerations

### Horizontal Scaling

- Stateless application design
- JWT-based authentication (no session store)
- Database connection pooling
- Ready for load balancer deployment

### Vertical Scaling

- Configurable thread pools
- Database connection pool sizing
- JVM memory tuning
- Redis max memory policies

### Future Enhancements

- Database read replicas
- Microservices decomposition
- Event-driven architecture with Kafka
- API gateway integration
- Service mesh (Istio)

## Monitoring & Observability

### Actuator Endpoints

- `/actuator/health` - Health checks
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics

### Logging Strategy

- Structured logging
- Different levels per package
- Production-ready log aggregation ready
- Correlation IDs for request tracing

## Deployment Architecture

### Docker Deployment

```
┌─────────────────────────────────────┐
│         Load Balancer               │
└────────────┬────────────────────────┘
             │
    ┌────────┴────────┐
    │                 │
┌───▼────┐      ┌────▼────┐
│  App   │      │  App    │
│Instance│      │Instance │
└───┬────┘      └────┬────┘
    │                │
    └────────┬───────┘
             │
    ┌────────┴─────────────────┐
    │                          │
┌───▼────┐  ┌────▼────┐  ┌────▼───┐
│Postgres│  │ MongoDB │  │ Redis  │
└────────┘  └─────────┘  └────────┘
```

### Environment Configuration

- Development: Local Docker Compose
- Staging: Kubernetes cluster
- Production: Cloud-managed services

## Performance Optimizations

### Database

- Proper indexing on frequently queried columns
- Connection pooling (HikariCP)
- Query optimization with EXPLAIN
- Lazy loading for relationships

### Caching

- Redis for AI responses
- Application-level caching
- HTTP response caching (future)

### API

- Pagination for list endpoints (future)
- Field filtering (future)
- Compression enabled
- Keep-alive connections

## Code Quality Measures

### Static Analysis
- Maven compiler with strict warnings
- Lombok annotation processing
- MapStruct compile-time validation

### Best Practices
- SOLID principles
- DRY (Don't Repeat Yourself)
- Clear separation of concerns
- Meaningful naming conventions
- Comprehensive JavaDoc (future)

## Conclusion

This architecture provides:
- **Scalability**: Horizontal and vertical scaling ready
- **Maintainability**: Clear separation of concerns
- **Testability**: Multiple layers of testing
- **Security**: Industry-standard practices
- **Performance**: Optimized data access and caching
- **Flexibility**: Easy to extend and modify

The design demonstrates enterprise-grade Java development with modern Spring Boot practices, making it suitable for production deployment and future enhancements.
