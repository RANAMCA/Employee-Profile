# Testing Guide

## Overview

This project includes comprehensive automated testing with unit tests, integration tests, and CI/CD pipeline validation.

## Test Coverage

- **Unit Tests**: Service layer business logic
- **Integration Tests**: End-to-end API testing with real databases
- **Code Coverage**: Target 60%+ line coverage (enforced by JaCoCo)
- **Static Analysis**: Checkstyle, SpotBugs, OWASP Dependency Check

## Running Tests Locally

### Prerequisites

Ensure you have the following running:
- PostgreSQL (port 5432)
- MongoDB (port 27017)
- Redis (port 6379)

Or use Docker Compose:
```bash
docker-compose up -d postgres mongodb redis
```

### Run All Tests

```bash
# Run unit tests only
mvn test

# Run integration tests only
mvn verify -Pit

# Run all tests with coverage
mvn clean verify

# Skip tests
mvn clean package -DskipTests
```

### Generate Test Reports

```bash
# Generate test coverage report
mvn jacoco:report

# View coverage report
open target/site/jacoco/index.html  # macOS/Linux
start target\site\jacoco\index.html # Windows

# Generate Surefire test report
mvn surefire-report:report

# View test report
open target/site/surefire-report.html
```

## Test Structure

```
src/test/java/com/newwork/employeeprofile/
├── integration/           # Integration tests (*IT.java)
│   ├── AuthControllerIT.java
│   ├── EmployeeControllerIT.java
│   └── BaseIntegrationTest.java
└── service/              # Unit tests (*Test.java)
    ├── AuthServiceTest.java
    ├── AbsenceServiceTest.java
    ├── EmployeeServiceTest.java
    └── FeedbackServiceTest.java
```

## Code Quality Checks

### Checkstyle

```bash
# Run Checkstyle
mvn checkstyle:check

# Generate Checkstyle report
mvn checkstyle:checkstyle
open target/site/checkstyle.html
```

### SpotBugs

```bash
# Run SpotBugs
mvn spotbugs:check

# Generate SpotBugs report
mvn spotbugs:spotbugs
open target/site/spotbugs.html
```

### OWASP Dependency Check

```bash
# Run dependency vulnerability scan
mvn dependency-check:check

# View report
open target/dependency-check-report.html
```

## CI/CD Pipeline

### GitHub Actions Workflow

The CI/CD pipeline runs automatically on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`

### Pipeline Jobs

1. **Test** (runs in parallel)
   - Unit tests with JaCoCo coverage
   - Coverage uploaded to Codecov
   - Test results uploaded as artifacts

2. **Build**
   - Maven package (JAR artifact)
   - Artifact uploaded for deployment

3. **Code Quality**
   - Checkstyle analysis
   - SpotBugs static analysis
   - Optional SonarCloud integration

4. **Docker**
   - Docker image build validation
   - Cache optimization with GitHub Actions cache

5. **Security Scan**
   - Trivy filesystem vulnerability scan
   - OWASP dependency check
   - Results uploaded to GitHub Security tab

6. **Integration Tests**
   - Full integration test suite
   - Uses PostgreSQL, MongoDB, Redis service containers
   - Test results uploaded as artifacts

### Viewing Pipeline Results

1. Go to your repository on GitHub
2. Click **Actions** tab
3. View workflow runs and logs
4. Download test artifacts if needed

### Pipeline Configuration

Edit `.github/workflows/ci.yml` to customize:
- Test databases (service containers)
- Coverage thresholds
- Artifact retention
- Deployment steps

## Test Database Configuration

### Unit Tests (In-Memory)

Unit tests use H2 in-memory database:
```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Integration Tests (Service Containers)

Integration tests use real databases via Docker or GitHub Actions service containers.

## Writing Tests

### Unit Test Example

```java
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    @Test
    void getEmployeeById_Success() {
        // Arrange
        Employee employee = new Employee();
        employee.setId(1L);
        when(employeeRepository.findById(1L))
            .thenReturn(Optional.of(employee));

        // Act
        EmployeeDto result = employeeService.getEmployeeById(1L, userPrincipal);

        // Assert
        assertNotNull(result);
        verify(employeeRepository).findById(1L);
    }
}
```

### Integration Test Example

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Testcontainers
class EmployeeControllerIT extends BaseIntegrationTest {

    @Test
    void getAllEmployees_ReturnsEmployeeList() {
        // Arrange
        String token = authenticateAndGetToken("test@test.com", "password123");

        // Act
        ResponseEntity<List<EmployeeDto>> response = restTemplate.exchange(
            "/api/employees",
            HttpMethod.GET,
            createAuthEntity(token),
            new ParameterizedTypeReference<>() {}
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
```

## Coverage Requirements

### Minimum Coverage (enforced by JaCoCo)

- **Line Coverage**: 60%
- **Branch Coverage**: No minimum (recommended 50%+)
- **Method Coverage**: No minimum

### Improving Coverage

Focus on testing:
1. Service layer business logic
2. Critical path scenarios
3. Error handling and edge cases
4. Validation logic
5. Access control rules

## Continuous Improvement

### Adding New Tests

1. Write test for new feature
2. Run tests locally: `mvn test`
3. Check coverage: `mvn jacoco:report`
4. Commit and push (CI runs automatically)
5. Review coverage report in PR

### Test Maintenance

- Keep tests fast (< 5 seconds per test)
- Use test fixtures for common data
- Mock external dependencies
- Clean up test data after each test
- Update tests when changing business logic

## Troubleshooting

### Tests Fail Locally

```bash
# Clean and rebuild
mvn clean verify

# Check database connections
docker-compose ps

# Restart databases
docker-compose restart postgres mongodb redis

# View logs
mvn test -X  # Debug mode
```

### CI/CD Pipeline Fails

1. Check GitHub Actions logs
2. Verify service container health
3. Check for dependency conflicts
4. Review test environment variables
5. Compare local vs CI database versions

### Coverage Too Low

```bash
# Identify uncovered code
mvn jacoco:report
open target/site/jacoco/index.html

# Focus on:
# - Red/yellow highlighted lines
# - Untested branches
# - New code without tests
```

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [Testcontainers](https://www.testcontainers.org/)
- [JaCoCo Maven Plugin](https://www.jacoco.org/jacoco/trunk/doc/maven.html)

---

**Last Updated**: 2025-12-11
