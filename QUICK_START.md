# Quick Start Guide

## Running the Application Locally (5 minutes)

### Option 1: Docker Compose (Recommended)

```bash
# Clone and navigate
git clone https://github.com/RANAMCA/Employee-Profile.git
cd Employee-Profile

# Start everything (first time - includes build)
docker-compose up --build

# Wait for "Started EmployeeProfileApplication" message
# Access Swagger UI: http://localhost:8080/swagger-ui.html

# Or run in background:
# docker-compose up --build -d
# docker-compose logs -f app  # to view logs
```

### Option 2: Local Development

```bash
# Start databases only
docker-compose up postgres mongodb redis -d

# Run application
mvn spring-boot:run

# Access Swagger UI: http://localhost:8080/swagger-ui.html
```

## Test the API in 3 Steps

### Step 1: Login as Manager
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.manager@newwork.com",
    "password": "password"
  }'
```

Save the `token` from the response.

### Step 2: Get All Employees
```bash
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Step 3: Create Absence Request
Login as employee first, then:
```bash
curl -X POST http://localhost:8080/api/absences \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "VACATION",
    "startDate": "2024-12-25",
    "endDate": "2024-12-31",
    "reason": "Holiday vacation"
  }'
```

## Running Tests

```bash
# All tests
mvn test

# Integration tests only
mvn test -Dtest=**/*IT

# Skip tests during build
mvn clean install -DskipTests
```

## Default Users

| Email                        | Password  | Role      |
|------------------------------|-----------|-----------|
| john.manager@newwork.com     | password  | MANAGER   |
| jane.smith@newwork.com       | password  | EMPLOYEE  |
| bob.coworker@newwork.com     | password  | COWORKER  |

## Key Endpoints

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Health Check**: http://localhost:8080/actuator/health

## Troubleshooting

### Port Already in Use
```bash
# Check what's using port 8080
netstat -ano | findstr :8080

# Stop the application
docker-compose down
```

### Database Connection Issues
```bash
# Restart databases
docker-compose restart postgres mongodb redis

# Check logs
docker-compose logs postgres
```

### Build Fails
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if needed
mvn clean install -DskipTests
```

## Next Steps

1. Explore the Swagger UI to see all available endpoints
2. Test different user roles and their permissions
3. Try the AI-enhanced feedback feature
4. Run the integration tests
5. Check out the comprehensive README.md

Enjoy exploring the application!
