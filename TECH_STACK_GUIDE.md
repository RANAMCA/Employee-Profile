# Complete Tech Stack Guide - Employee Profile HR Application

## Table of Contents
1. [Overview](#overview)
2. [Core Technologies](#core-technologies)
3. [MapStruct Deep Dive](#mapstruct-deep-dive)
4. [Lombok Deep Dive](#lombok-deep-dive)
5. [Spring Boot Framework](#spring-boot-framework)
6. [Security & JWT](#security--jwt)
7. [Multi-Database Strategy](#multi-database-strategy)
8. [Validation Framework](#validation-framework)
9. [API Documentation (OpenAPI)](#api-documentation-openapi)
10. [Complete Request Flow Examples](#complete-request-flow-examples)
11. [Caching Strategy](#caching-strategy)
12. [Exception Handling](#exception-handling)

---

## Overview

This is an **Enterprise-grade HR Management System** built with:
- **Java 21** (Latest LTS with modern features)
- **Spring Boot 3.2.0** (Latest Spring ecosystem)
- **Multi-database architecture** (PostgreSQL + MongoDB + Redis)
- **JWT-based stateless authentication**
- **Role-based access control (RBAC)**
- **AI integration** for text enhancement

---

## Core Technologies

### 1. **Spring Boot 3.2.0**
**Purpose**: Application framework providing dependency injection, auto-configuration, and production-ready features

**Key Components Used**:
```
spring-boot-starter-web         → REST API development
spring-boot-starter-data-jpa    → PostgreSQL database access
spring-boot-starter-data-mongodb → MongoDB database access
spring-boot-starter-data-redis  → Redis caching
spring-boot-starter-security    → Authentication & Authorization
spring-boot-starter-validation  → Bean validation
spring-boot-starter-actuator    → Production monitoring
spring-boot-devtools            → Hot reload during development
```

### 2. **Java 21**
**Modern Features Used**:
- **Records**: For immutable data carriers (can be used in DTOs)
- **Pattern Matching**: Cleaner instanceof checks
- **Text Blocks**: Multi-line strings for SQL/JSON
- **Sealed Classes**: Restricted inheritance hierarchies

### 3. **PostgreSQL 15+**
**Purpose**: Primary relational database for structured data

**Why PostgreSQL?**
- ACID compliance for critical data (employees, roles, departments)
- Complex relationships with foreign keys
- Advanced indexing capabilities
- Strong data integrity

**Tables**:
- `employees` - Employee profiles
- `roles` - User roles (EMPLOYEE, MANAGER, ADMIN, HR)
- `departments` - Organizational units
- `permissions` - Fine-grained access control
- `absences` - Time-off requests

### 4. **MongoDB**
**Purpose**: Document database for flexible, unstructured data

**Why MongoDB?**
- Schema flexibility for feedback documents
- High write throughput for feedback operations
- Easy horizontal scaling
- Natural fit for JSON-like data

**Collections**:
- `feedback` - Employee feedback documents with varying structures

### 5. **Redis**
**Purpose**: In-memory cache for performance optimization

**Usage**:
- AI response caching (1-hour TTL)
- Future: Session management, rate limiting

### 6. **Maven**
**Purpose**: Build tool and dependency management

**Key Plugins**:
- `maven-compiler-plugin` - Compiles Java code with annotation processors
- `spring-boot-maven-plugin` - Creates executable JAR
- `flyway-maven-plugin` - Database migrations

---

## MapStruct Deep Dive

### What is MapStruct?

**MapStruct** is a **compile-time code generator** that creates type-safe bean mappers. It generates implementation code during compilation, making it:
- **Fast** (no reflection)
- **Type-safe** (compile-time checks)
- **Easy to debug** (readable generated code)

### How MapStruct Works

#### 1. **Basic Mapper Interface**

```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
}
```

**What happens?**
1. **Compile Time**: MapStruct processor generates implementation class
2. **Runtime**: Spring detects `@Component` and creates bean
3. **Injection**: You can inject `EmployeeMapper` anywhere

#### 2. **Generated Implementation (What MapStruct Creates)**

```java
@Component
public class EmployeeMapperImpl implements EmployeeMapper {

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }

        EmployeeDto.EmployeeDtoBuilder employeeDto = EmployeeDto.builder();

        employeeDto.id(employee.getId());
        employeeDto.firstName(employee.getFirstName());
        employeeDto.lastName(employee.getLastName());
        employeeDto.email(employee.getEmail());
        employeeDto.role(roleMapper.toDto(employee.getRole()));
        employeeDto.department(departmentMapper.toDto(employee.getDepartment()));
        employeeDto.position(employee.getPosition());
        // ... all other fields

        return employeeDto.build();
    }
}
```

**Key Points**:
- ✅ **No reflection** - Direct method calls
- ✅ **Null-safe** - Automatic null checks
- ✅ **Nested mapping** - Uses other mappers for `Role` and `Department`
- ✅ **Builder pattern** - Works seamlessly with Lombok `@Builder`

#### 3. **Advanced MapStruct Features Used in Project**

##### **Feature 1: Field Mapping Control**

```java
@Mapping(target = "id", ignore = true)
@Mapping(target = "password", ignore = true)
@Mapping(target = "role", ignore = true)
@Mapping(target = "department", ignore = true)
@Mapping(target = "createdAt", ignore = true)
@Mapping(target = "updatedAt", ignore = true)
@Mapping(target = "version", ignore = true)
@Mapping(target = "active", constant = "true")
Employee toEntity(CreateEmployeeRequest request);
```

**Purpose**:
- `ignore = true` - Don't map these fields (will be set manually)
- `constant = "true"` - Always set `active = true` for new employees

##### **Feature 2: Update Existing Entity**

```java
@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Mapping(target = "id", ignore = true)
@Mapping(target = "password", ignore = true)
void updateEntityFromDto(UpdateEmployeeRequest dto, @MappingTarget Employee entity);
```

**Purpose**:
- `@MappingTarget` - Update existing entity instead of creating new one
- `IGNORE` strategy - Only update non-null fields from DTO

**Example**:
```java
// In Service Layer
Employee employee = employeeRepository.findById(id).orElseThrow();
employeeMapper.updateEntityFromDto(request, employee);
employeeRepository.save(employee);
```

##### **Feature 3: Using Other Mappers**

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    uses = {RoleMapper.class, DepartmentMapper.class}  // ← Uses other mappers
)
public interface EmployeeMapper {
    EmployeeDto toDto(Employee employee);
}
```

**What happens?**
When mapping `Employee.role` and `Employee.department`, MapStruct automatically calls:
- `roleMapper.toDto(employee.getRole())`
- `departmentMapper.toDto(employee.getDepartment())`

---

## Lombok Deep Dive

### What is Lombok?

**Lombok** is a **compile-time annotation processor** that generates boilerplate code automatically:
- Getters/Setters
- Constructors
- toString/equals/hashCode
- Builder pattern
- Logging

### Core Lombok Annotations Used

#### 1. **@Data**
```java
@Data
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
}
```

**Generates**:
```java
// Getter for all fields
public Long getId() { return id; }
public String getFirstName() { return firstName; }
public String getLastName() { return lastName; }

// Setter for all fields
public void setId(Long id) { this.id = id; }
public void setFirstName(String firstName) { this.firstName = firstName; }
public void setLastName(String lastName) { this.lastName = lastName; }

// toString()
public String toString() {
    return "EmployeeDto(id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ")";
}

// equals() and hashCode()
public boolean equals(Object o) { ... }
public int hashCode() { ... }
```

#### 2. **@Builder** (Most Important!)

```java
@Builder
public class EmployeeDto {
    private Long id;
    private String firstName;
    private String lastName;
    private RoleDto role;
    private DepartmentDto department;
}
```

**Generates Builder Pattern**:
```java
public class EmployeeDto {

    public static EmployeeDtoBuilder builder() {
        return new EmployeeDtoBuilder();
    }

    public static class EmployeeDtoBuilder {
        private Long id;
        private String firstName;
        private String lastName;
        private RoleDto role;
        private DepartmentDto department;

        public EmployeeDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EmployeeDtoBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public EmployeeDtoBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public EmployeeDtoBuilder role(RoleDto role) {
            this.role = role;
            return this;
        }

        public EmployeeDtoBuilder department(DepartmentDto department) {
            this.department = department;
            return this;
        }

        public EmployeeDto build() {
            return new EmployeeDto(id, firstName, lastName, role, department);
        }
    }
}
```

**Usage Example**:
```java
// Fluent, readable object construction
EmployeeDto dto = EmployeeDto.builder()
    .id(1L)
    .firstName("John")
    .lastName("Doe")
    .role(roleDto)
    .department(departmentDto)
    .build();
```

**Why Builder Pattern?**
- ✅ Immutability (if used with `@Value`)
- ✅ Optional parameters (don't need to set all fields)
- ✅ Readable code
- ✅ Type-safe construction

#### 3. **@NoArgsConstructor & @AllArgsConstructor**

```java
@NoArgsConstructor  // Default constructor
@AllArgsConstructor // Constructor with all fields
public class EmployeeDto {
    private Long id;
    private String firstName;
}
```

**Generates**:
```java
// No-args constructor (required by JPA, Jackson)
public EmployeeDto() {}

// All-args constructor
public EmployeeDto(Long id, String firstName) {
    this.id = id;
    this.firstName = firstName;
}
```

**Why Both?**
- `@NoArgsConstructor` - Required by Jackson (JSON deserialization) and JPA
- `@AllArgsConstructor` - Works with `@Builder` pattern

#### 4. **@Getter & @Setter** (JPA Entities)

```java
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;
}
```

**Why @Getter/@Setter instead of @Data?**
- JPA entities should be mutable (Hibernate proxy requirements)
- Avoid issues with `equals()`/`hashCode()` on lazy-loaded fields
- Better control over mutability

#### 5. **@Slf4j** (Logging)

```java
@Slf4j
@Service
public class EmployeeService {

    public void doSomething() {
        log.info("Processing employee");
        log.error("Error occurred: {}", errorMessage);
        log.debug("Debug info: {}", debugInfo);
    }
}
```

**Generates**:
```java
private static final org.slf4j.Logger log =
    org.slf4j.LoggerFactory.getLogger(EmployeeService.class);
```

#### 6. **@RequiredArgsConstructor** (Dependency Injection)

```java
@Service
@RequiredArgsConstructor  // ← Constructor injection
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final DepartmentService departmentService;

    // No need to write constructor!
}
```

**Generates**:
```java
public EmployeeService(
    EmployeeRepository employeeRepository,
    EmployeeMapper employeeMapper,
    DepartmentService departmentService) {

    this.employeeRepository = employeeRepository;
    this.employeeMapper = employeeMapper;
    this.departmentService = departmentService;
}
```

**Why This Pattern?**
- ✅ Spring recommends constructor injection over `@Autowired`
- ✅ Immutable dependencies (final fields)
- ✅ Easy to test (pass mocks in constructor)
- ✅ Compile-time safety

---

## Spring Boot Framework

### 1. **Dependency Injection (IoC)**

**How it Works**:
```java
@Service  // ← Spring creates instance and manages lifecycle
@RequiredArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;  // ← Spring injects this
}
```

**Bean Creation Flow**:
```
1. Spring scans @Component, @Service, @Repository, @Controller
2. Creates instances (singleton by default)
3. Resolves dependencies
4. Injects dependencies via constructor
```

### 2. **Spring Data JPA**

**Repository Pattern**:
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Method name → SQL query (automatic!)
    List<Employee> findByDepartmentId(Long departmentId);

    // Custom query
    @Query("SELECT e FROM Employee e WHERE e.active = true")
    List<Employee> findAllActive();

    // Native SQL
    @Query(value = "SELECT * FROM employees WHERE email = ?1", nativeQuery = true)
    Optional<Employee> findByEmail(String email);
}
```

**What Spring Generates**:
```java
// For findByDepartmentId(Long departmentId)
SELECT e FROM Employee e WHERE e.department.id = :departmentId
```

### 3. **Transaction Management**

```java
@Transactional  // ← Automatic transaction management
public EmployeeDto updateEmployee(Long id, UpdateEmployeeRequest request) {
    Employee employee = employeeRepository.findById(id).orElseThrow();
    employeeMapper.updateEntityFromDto(request, employee);

    // If exception occurs, transaction rolls back
    employee = employeeRepository.save(employee);

    return employeeMapper.toDto(employee);
}
```

**Transaction Flow**:
```
1. Method called → Transaction begins
2. Execute method logic
3. If success → Commit transaction
4. If exception → Rollback transaction
```

### 4. **Spring Security**

**Key Components**:

```java
@Configuration
@EnableMethodSecurity  // ← Enable @PreAuthorize
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http
            .csrf(AbstractHttpConfigurer::disable)  // Disabled for stateless JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

**Method-Level Security**:
```java
@PreAuthorize("hasAuthority('EMPLOYEE:READ:ALL')")
public ResponseEntity<List<EmployeeDto>> getAllEmployees() {
    // Only users with EMPLOYEE:READ:ALL permission can call this
}
```

---

## Security & JWT

### JWT (JSON Web Token) Flow

#### **1. User Login**

```
Client                     Server
  |                           |
  |------ POST /api/auth/login ----|
  |  { email, password }      |
  |                           |
  |                      [Validate credentials]
  |                      [Generate JWT]
  |                           |
  |<----- 200 OK -------------|
  |  { accessToken, refreshToken, user }
  |                           |
```

#### **2. JWT Structure**

A JWT has 3 parts separated by dots:
```
eyJhbGci...  .  eyJzdWIi...  .  SflKxwRJ...
   Header         Payload        Signature
```

**Header**:
```json
{
  "alg": "HS512",
  "typ": "JWT"
}
```

**Payload** (Claims):
```json
{
  "sub": "123",              // Employee ID
  "email": "john@test.com",
  "type": "access",
  "iat": 1701234567,         // Issued at
  "exp": 1701238167          // Expiration (1 hour)
}
```

**Signature**:
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret-key
)
```

#### **3. Using JWT in Requests**

```
Client                     Server
  |                           |
  |-- GET /api/employees -----|
  |  Authorization: Bearer eyJhbGci...
  |                           |
  |                      [JwtAuthenticationFilter]
  |                      ↓
  |                      [Extract token]
  |                      [Validate signature]
  |                      [Extract employee ID]
  |                      ↓
  |                      [Load UserPrincipal]
  |                      [Set SecurityContext]
  |                      ↓
  |                      [Controller method]
  |                           |
  |<---- 200 OK --------------|
  |  [Employee data]          |
```

#### **4. JWT Implementation**

**Token Generation**:
```java
public String generateToken(Long employeeId, String email, boolean isRefresh) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

    return Jwts.builder()
        .subject(employeeId.toString())      // Employee ID
        .claim("email", email)               // Email
        .claim("type", "access")             // Token type
        .issuedAt(now)                       // Issue time
        .expiration(expiryDate)              // Expiration
        .signWith(secretKey)                 // Sign with HS512
        .compact();                          // Build token
}
```

**Token Validation**:
```java
public boolean validateToken(String authToken) {
    try {
        Jwts.parser()
            .verifyWith(secretKey)           // Verify signature
            .build()
            .parseSignedClaims(authToken);   // Parse claims
        return true;
    } catch (ExpiredJwtException ex) {
        log.error("Expired JWT token");
        return false;
    }
}
```

**Extract User ID**:
```java
public Long getEmployeeIdFromToken(String token) {
    Claims claims = Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();

    return Long.parseLong(claims.getSubject());
}
```

#### **5. Authentication Filter**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) {

        // 1. Extract token from Authorization header
        String token = getJwtFromRequest(request);

        // 2. Validate token
        if (token != null && jwtTokenProvider.validateToken(token)) {

            // 3. Get employee ID from token
            Long employeeId = jwtTokenProvider.getEmployeeIdFromToken(token);

            // 4. Load user details
            UserDetails userDetails = userDetailsService.loadUserById(employeeId);

            // 5. Create authentication object
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );

            // 6. Set in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 7. Continue filter chain
        filterChain.doFilter(request, response);
    }
}
```

---

## Multi-Database Strategy

### Database Selection Logic

```
┌─────────────────────────────────────────┐
│         Data Type Decision Tree         │
└─────────────────────────────────────────┘
                   │
                   ▼
        Need ACID transactions?
                   │
        ┌──────────┴──────────┐
       YES                    NO
        │                      │
        ▼                      ▼
  Need relationships?    Schema flexible?
        │                      │
       YES                    YES
        │                      │
        ▼                      ▼
   PostgreSQL              MongoDB


Need high-speed cache?
        │
       YES
        │
        ▼
      Redis
```

### PostgreSQL - Relational Data

**Entities**:
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;  // ← Foreign key relationship

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;  // ← Foreign key relationship
}
```

**Why PostgreSQL?**
- Foreign keys enforce referential integrity
- Complex joins (employee + role + department)
- ACID transactions for critical operations
- Indexes on frequently queried columns

**Repository**:
```java
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Spring Data JPA generates SQL
}
```

### MongoDB - Document Data

**Document**:
```java
@Document(collection = "feedback")
public class Feedback {
    @Id
    private String id;  // MongoDB ObjectId

    private Long employeeId;
    private Long recipientId;
    private String content;
    private String polishedContent;  // AI-enhanced version
    private LocalDateTime createdAt;

    // No rigid schema - can add fields dynamically
}
```

**Why MongoDB?**
- Feedback structure can vary over time
- High write throughput
- No complex relationships needed
- Easy to scale horizontally

**Repository**:
```java
@Repository
public interface FeedbackRepository extends MongoRepository<Feedback, String> {
    // Spring Data MongoDB generates queries
}
```

### Redis - Caching

**Usage**:
```java
@Service
public class AIService {

    @Cacheable(value = "ai-responses", key = "#content")
    public String polishFeedback(String content) {
        // If cached, returns from Redis
        // If not cached, calls HuggingFace API and caches result
        return callHuggingFaceAPI(content);
    }
}
```

**Configuration**:
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))  // 1-hour TTL
            .disableCachingNullValues();
    }
}
```

---

## Validation Framework

### Bean Validation (Jakarta Validation)

**DTO with Validation**:
```java
public class CreateEmployeeRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be 2-50 characters")
    private String firstName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
        message = "Password must contain uppercase, lowercase, and digit"
    )
    private String password;

    @NotNull(message = "Role ID is required")
    private Long roleId;
}
```

**Controller Validation**:
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody CreateEmployeeRequest request) {
    // @Valid triggers validation
    // If validation fails → 400 Bad Request with error details
}
```

**Validation Error Response**:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "firstName": "First name is required",
    "email": "Email should be valid",
    "password": "Password must be at least 8 characters"
  }
}
```

**Custom Validators**:
```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueEmailValidator.class)
public @interface UniqueEmail {
    String message() default "Email already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

## API Documentation (OpenAPI)

### Swagger/OpenAPI Integration

**Configuration**:
```java
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Employee Profile API")
                .version("1.0.0")
                .description("HR Management System with RBAC"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

**Controller Documentation**:
```java
@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employees", description = "Employee management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class EmployeeController {

    @GetMapping("/{id}")
    @Operation(
        summary = "Get employee by ID",
        description = "Retrieves employee details based on role permissions"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Success"),
        @ApiResponse(responseCode = "404", description = "Employee not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        // ...
    }
}
```

**Access Swagger UI**:
```
http://localhost:8080/swagger-ui.html
```

---

## Complete Request Flow Examples

### Example 1: User Registration

**Request**:
```http
POST /api/auth/register
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "password": "SecurePass123",
  "roleId": 1,
  "departmentId": 2,
  "position": "Software Engineer"
}
```

**Flow**:
```
1. HTTP Request → DispatcherServlet (Spring MVC)
   ↓
2. AuthController.register()
   ↓
3. @Valid annotation triggers Bean Validation
   - Validates @NotBlank, @Email, @Size, etc.
   - If fails → 400 Bad Request
   ↓
4. AuthService.register(request)
   ↓
5. Check if email exists
   - employeeRepository.existsByEmail(email)
   - If exists → throw BadRequestException
   ↓
6. Get Role entity from database
   - roleService.getRoleEntityById(roleId)
   ↓
7. Get Department entity from database
   - departmentService.getDepartmentEntityById(departmentId)
   ↓
8. MapStruct: CreateEmployeeRequest → Employee
   - employeeMapper.toEntity(request)
   - Generated code copies all matching fields
   ↓
9. Hash password
   - passwordEncoder.encode(password)
   ↓
10. Set role and department (ignored by mapper)
   - employee.setRole(role)
   - employee.setDepartment(department)
   ↓
11. Save to PostgreSQL
   - employeeRepository.save(employee)
   - JPA generates INSERT statement
   - @CreatedDate sets createdAt
   - @LastModifiedDate sets updatedAt
   ↓
12. Generate JWT tokens
   - jwtTokenProvider.generateToken(employee)
   - jwtTokenProvider.generateRefreshToken(employee)
   ↓
13. MapStruct: Employee → EmployeeDto
   - employeeMapper.toDto(employee)
   - Nested mappers: roleMapper.toDto(), departmentMapper.toDto()
   ↓
14. Build response
   - AuthResponse.builder()
       .accessToken(token)
       .refreshToken(refreshToken)
       .user(employeeDto)
       .build()
   ↓
15. Jackson serializes to JSON
   ↓
16. HTTP Response 201 Created
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 123,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "role": {
      "id": 1,
      "name": "EMPLOYEE",
      "description": "Regular employee"
    },
    "department": {
      "id": 2,
      "name": "Engineering",
      "description": "Software Development"
    },
    "position": "Software Engineer"
  }
}
```

---

### Example 2: Get All Employees (with RBAC)

**Request**:
```http
GET /api/employees
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Flow**:
```
1. HTTP Request → DispatcherServlet
   ↓
2. JwtAuthenticationFilter intercepts
   ↓
3. Extract JWT from Authorization header
   - String token = request.getHeader("Authorization")
       .replace("Bearer ", "")
   ↓
4. Validate JWT signature
   - jwtTokenProvider.validateToken(token)
   - Parses JWT, verifies HMAC signature
   - Checks expiration
   ↓
5. Extract employee ID from JWT
   - Long employeeId = jwtTokenProvider.getEmployeeIdFromToken(token)
   - Gets "sub" claim from JWT payload
   ↓
6. Load user details from database
   - CustomUserDetailsService.loadUserById(employeeId)
   - Queries: SELECT * FROM employees WHERE id = ?
   - Eagerly loads Role (FETCH EAGER)
   - Loads permissions from role
   ↓
7. Create UserPrincipal
   - UserPrincipal.create(employee)
   - Wraps employee + role + permissions
   ↓
8. Set SecurityContext
   - UsernamePasswordAuthenticationToken auth = new ...
   - SecurityContextHolder.getContext().setAuthentication(auth)
   ↓
9. Filter chain continues → Controller
   ↓
10. @PreAuthorize check
   - @PreAuthorize("hasAnyAuthority('EMPLOYEE:READ:ALL', 'EMPLOYEE:READ:DEPARTMENT', 'EMPLOYEE:READ:OWN')")
   - Checks UserPrincipal.getAuthorities()
   - If no matching authority → 403 Forbidden
   ↓
11. EmployeeController.getAllEmployees(@AuthenticationPrincipal UserPrincipal currentUser)
   - Spring injects UserPrincipal from SecurityContext
   ↓
12. EmployeeService.getAllEmployees(currentUser)
   ↓
13. Check permission scope (RBAC logic)

   IF user has EMPLOYEE:READ:ALL permission:
      → Query: SELECT * FROM employees WHERE active = true

   ELSE IF user has EMPLOYEE:READ:DEPARTMENT permission:
      → Get user's department
      → Query: SELECT * FROM employees WHERE department_id = ?

   ELSE IF user has EMPLOYEE:READ:OWN permission:
      → Return only current user's profile

   ELSE:
      → throw UnauthorizedException
   ↓
14. MapStruct: List<Employee> → List<EmployeeDto>
   - employees.stream().map(employeeMapper::toDto).toList()
   - For each employee:
     * Maps all fields
     * Calls roleMapper.toDto(employee.getRole())
     * Calls departmentMapper.toDto(employee.getDepartment())
   ↓
15. @JsonView filters fields
   - @JsonView(EmployeeDto.Views.Extended.class)
   - Jackson only serializes fields with @JsonView(Views.Extended)
   - Hides sensitive fields (e.g., password)
   ↓
16. Jackson serializes to JSON
   ↓
17. HTTP Response 200 OK
```

**Response (for EMPLOYEE role)**:
```json
[
  {
    "id": 5,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@company.com",
    "role": {
      "id": 1,
      "name": "EMPLOYEE"
    },
    "department": {
      "id": 2,
      "name": "Engineering"
    },
    "position": "Software Engineer",
    "phone": "+1234567890",
    "hireDate": "2024-01-15",
    "bio": "Passionate developer",
    "skills": "Java, Spring Boot, React"
  }
]
```

**Response (for MANAGER role - sees all employees)**:
```json
[
  {
    "id": 1,
    "firstName": "Alice",
    "lastName": "Manager",
    ...
  },
  {
    "id": 2,
    "firstName": "Bob",
    "lastName": "Developer",
    ...
  },
  ...
]
```

---

### Example 3: Update Employee (with Partial Update)

**Request**:
```http
PUT /api/employees/5
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "phone": "+9876543210",
  "bio": "Senior Software Engineer with 5 years of experience"
}
```

**Flow**:
```
1. JWT Authentication (same as Example 2)
   ↓
2. EmployeeController.updateEmployee(5, request, currentUser)
   ↓
3. @Valid validates UpdateEmployeeRequest
   ↓
4. EmployeeService.updateEmployee(5, request, currentUser)
   ↓
5. Load employee from database
   - Employee employee = employeeRepository.findById(5).orElseThrow()
   ↓
6. Validate update permission

   IF user has EMPLOYEE:UPDATE:ALL:
      → Can update any employee

   ELSE IF user has EMPLOYEE:UPDATE:DEPARTMENT:
      → Check if target employee in same department
      → If not → throw UnauthorizedException

   ELSE IF user has EMPLOYEE:UPDATE:OWN:
      → Check if target employee is current user
      → If not → throw UnauthorizedException
   ↓
7. MapStruct partial update
   - employeeMapper.updateEntityFromDto(request, employee)

   Generated code:
   if (request.getPhone() != null) {
       employee.setPhone(request.getPhone());
   }
   if (request.getBio() != null) {
       employee.setBio(request.getBio());
   }
   // Other fields unchanged!
   ↓
8. JPA dirty checking
   - Hibernate detects changed fields
   - @LastModifiedDate updates updatedAt
   - @Version increments version (optimistic locking)
   ↓
9. Save to database
   - employeeRepository.save(employee)
   - JPA generates: UPDATE employees SET phone = ?, bio = ?, updated_at = ?, version = ? WHERE id = ? AND version = ?
   ↓
10. MapStruct: Employee → EmployeeDto
   ↓
11. HTTP Response 200 OK
```

**Response**:
```json
{
  "id": 5,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "phone": "+9876543210",
  "bio": "Senior Software Engineer with 5 years of experience",
  "updatedAt": "2024-12-07T10:30:00"
}
```

**Key Points**:
- ✅ Only specified fields updated
- ✅ Null values ignored (NullValuePropertyMappingStrategy.IGNORE)
- ✅ Optimistic locking prevents concurrent updates
- ✅ Audit fields automatically updated

---

### Example 4: Submit Feedback with AI Polish

**Request**:
```http
POST /api/feedbacks
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json

{
  "recipientId": 10,
  "content": "John did great work on the project",
  "polishWithAI": true
}
```

**Flow**:
```
1. JWT Authentication
   ↓
2. FeedbackController.createFeedback(request, currentUser)
   ↓
3. FeedbackService.createFeedback(request, currentUser)
   ↓
4. Validate recipient exists
   - employeeRepository.findById(10).orElseThrow()
   ↓
5. Check if AI polishing requested

   IF polishWithAI == true:
      ↓
      AIService.polishFeedback(content)
      ↓
      @Cacheable checks Redis cache
      - Key: hash of content

      IF found in cache:
         → Return cached response

      ELSE:
         → Call HuggingFace API
         → Cache response in Redis (TTL: 1 hour)
         → Return polished content
   ↓
6. Build Feedback document
   - Feedback feedback = Feedback.builder()
       .employeeId(currentUser.getId())
       .recipientId(recipientId)
       .content(originalContent)
       .polishedContent(polishedContent)  // AI-enhanced
       .createdAt(LocalDateTime.now())
       .build()
   ↓
7. Save to MongoDB
   - feedbackRepository.save(feedback)
   - MongoDB inserts document
   - Generates ObjectId
   ↓
8. MapStruct: Feedback → FeedbackDto
   ↓
9. HTTP Response 201 Created
```

**Response**:
```json
{
  "id": "507f1f77bcf86cd799439011",
  "employeeId": 5,
  "recipientId": 10,
  "content": "John did great work on the project",
  "polishedContent": "John demonstrated exceptional performance and delivered outstanding results throughout the project lifecycle. His contributions significantly impacted the team's success.",
  "createdAt": "2024-12-07T10:45:00"
}
```

---

## Caching Strategy

### Redis Caching

**Configuration**:
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))  // 1-hour TTL
            .disableCachingNullValues()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .build();
    }
}
```

**Cache Usage**:
```java
@Service
public class AIService {

    // Cache AI responses to avoid redundant API calls
    @Cacheable(value = "ai-responses", key = "#content")
    public String polishFeedback(String content) {
        return callHuggingFaceAPI(content);
    }

    // Evict cache entry
    @CacheEvict(value = "ai-responses", key = "#content")
    public void invalidateCache(String content) {
        // Cache entry removed
    }

    // Update cache
    @CachePut(value = "ai-responses", key = "#content")
    public String updateCache(String content, String newValue) {
        return newValue;
    }
}
```

**Cache Flow**:
```
Request: polishFeedback("John did great work")
   ↓
Check Redis: GET ai-responses::John did great work
   ↓
IF found:
   → Return from cache (instant response)
ELSE:
   → Call HuggingFace API (slow)
   → Store in Redis: SET ai-responses::John did great work "polished text"
   → Return result
```

---

## Exception Handling

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.UNAUTHORIZED.value())
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );

        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .message("Validation failed")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        return ResponseEntity.badRequest().body(error);
    }
}
```

**Custom Exceptions**:
```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
```

---

## Key Takeaways for Interviews

### 1. **MapStruct vs Manual Mapping**
**Question**: "Why use MapStruct?"
**Answer**:
- ✅ Compile-time code generation (no reflection overhead)
- ✅ Type-safe (compile errors if fields don't match)
- ✅ Easy to debug (can view generated implementation)
- ✅ Integrates with Lombok builders
- ✅ Supports complex mappings and nested objects

### 2. **Lombok Benefits**
**Question**: "Why use Lombok?"
**Answer**:
- ✅ Reduces boilerplate by 60-70%
- ✅ Cleaner, more readable code
- ✅ Builder pattern for immutability
- ✅ Constructor injection with @RequiredArgsConstructor
- ✅ Automatic logging with @Slf4j

### 3. **Multi-Database Strategy**
**Question**: "Why three databases?"
**Answer**:
- **PostgreSQL**: ACID transactions, relational integrity (employees, roles)
- **MongoDB**: Flexible schema, high throughput (feedback documents)
- **Redis**: In-memory caching, sub-millisecond latency (AI responses)

### 4. **JWT vs Session**
**Question**: "Why JWT over sessions?"
**Answer**:
- ✅ Stateless (no server-side session storage)
- ✅ Scalable (works across multiple servers)
- ✅ Mobile-friendly (no cookies needed)
- ✅ Contains user info (no database lookup per request)

### 5. **RBAC Implementation**
**Question**: "How does role-based access work?"
**Answer**:
- Permissions stored as: `RESOURCE:ACTION:SCOPE` (e.g., `EMPLOYEE:READ:ALL`)
- JWT contains user ID → Load user + role + permissions
- `@PreAuthorize` checks authorities
- Service layer validates scope (ALL, DEPARTMENT, OWN)

### 6. **Performance Optimization**
**Question**: "How did you optimize performance?"
**Answer**:
- ✅ Redis caching for AI responses
- ✅ Database indexing on frequently queried columns
- ✅ Lazy loading for non-critical relationships
- ✅ MapStruct for fast object mapping
- ✅ Connection pooling (HikariCP)

---

## Summary

This project demonstrates **production-ready Java/Spring Boot development** with:

✅ **Modern Java 21** features
✅ **Spring Boot 3.2** best practices
✅ **MapStruct** for type-safe mapping
✅ **Lombok** for clean code
✅ **JWT** stateless authentication
✅ **Multi-database** architecture
✅ **RBAC** for fine-grained access
✅ **Redis** caching for performance
✅ **OpenAPI** documentation
✅ **Bean Validation** for input safety
✅ **Global exception handling**

You're now equipped to explain every technology and data flow in your project!
