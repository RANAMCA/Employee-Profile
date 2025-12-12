# Agent Development Guidelines

## Build Commands
```bash
# Build project
mvn clean install

# Run single test
mvn test -Dtest=ClassName#methodName

# Run integration tests only  
mvn verify -Pit

# Run all tests with coverage
mvn clean verify

# Code quality checks
mvn checkstyle:check
mvn spotbugs:check
mvn dependency-check:check
```

## Code Style Guidelines

### Imports & Formatting
- Use Google Java Style Guide (enforced by checkstyle)
- Order imports: static, java, javax, org, com
- No wildcard imports except `static org.junit.jupiter.api.Assertions.*`
- Max line length: 100 characters

### Naming Conventions
- Classes: PascalCase (EmployeeService)
- Methods: camelCase (getAllEmployees) 
- Constants: UPPER_SNAKE_CASE (DEFAULT_PAGE_SIZE)
- Variables: camelCase (currentUser)
- Packages: lowercase with dots (com.newwork.employeeprofile)

### Types & Annotations
- Use Java 21+ features (records, switch expressions)
- Prefer `@RequiredArgsConstructor` over manual constructors
- Use Lombok annotations: `@Slf4j`, `@Getter`, `@Setter`, `@Builder`
- Entity classes: `@Entity`, `@Table`, JPA annotations
- Controllers: `@RestController`, `@RequestMapping`, OpenAPI annotations

### Error Handling
- Use custom exceptions from `exception` package
- Throw `ResourceNotFoundException` for missing entities
- Throw `BadRequestException` for validation errors
- Throw `UnauthorizedException` for permission issues
- Use `@ControllerAdvice` for global exception handling

### Security & Permissions
- All endpoints require JWT authentication
- Use `@PreAuthorize` with permission format: `RESOURCE:ACTION:SCOPE`
- Validate access in service layer, not just controllers
- Use `UserPrincipal` for authentication context
- Filter sensitive data based on user permissions

### Testing Patterns
- Unit tests: `*Test.java` with Mockito
- Integration tests: `*IT.java` extending `BaseIntegrationTest`
- Use Testcontainers for real database testing
- Test coverage minimum: 60% (enforced by JaCoCo)
- Mock external dependencies in unit tests

### Database & Transactions
- Use `@Transactional` with `readOnly` flag for queries
- Entity relationships: `@ManyToOne(fetch = LAZY/EAGER)`
- Use `@CreatedDate`/`@LastModifiedDate` for auditing
- Flyway migrations in `src/main/resources/db/migration/`

### API Design
- REST endpoints: `/api/{resource}` plural
- Use DTOs for request/response (separate packages)
- OpenAPI annotations for documentation
- Return `ResponseEntity` with appropriate HTTP status
- Use `@Valid` for request body validation