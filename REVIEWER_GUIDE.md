# Reviewer Guide

## Quick Start for Reviewers

This guide helps you quickly verify that the Employee Profile HR Management System works as expected.

## ✅ Pre-Review Checklist

### 1. Check CI/CD Status

Go to the **Actions** tab on GitHub to see:
- ✅ All pipeline jobs passing (Test, Build, Code Quality, Security)
- ✅ Test coverage report available
- ✅ No critical security vulnerabilities

If CI is green, the application compiles and tests pass!

### 2. Review Documentation

Read these in order:
1. **README.md** - Project overview and features
2. **QUICK_START.md** - Setup instructions
3. **ARCHITECTURE.md** - System design
4. **TESTING.md** - Testing strategy

## 🚀 Local Verification (Optional)

### Option A: Docker (Recommended - 5 minutes)

```bash
# Start all services
docker-compose up -d

# Wait 30 seconds for services to start

# Access application
open http://localhost:8080  # macOS
start http://localhost:8080 # Windows
```

**Test Credentials:**
- Employee: `test@test.com` / `password123`
- Manager: Create via registration form

### Option B: Manual Setup (15 minutes)

**Prerequisites:**
- Java 21
- PostgreSQL 15
- MongoDB 7
- Redis 7

```bash
# 1. Start databases
docker-compose up -d postgres mongodb redis

# 2. Build application
mvn clean package -DskipTests

# 3. Run application
java -jar target/employee-profile-1.0.0-SNAPSHOT.jar

# 4. Access at http://localhost:8080
```

## 📋 Feature Verification

### Core Features to Test

#### 1. Authentication & Authorization ✅
- [ ] Register new employee account
- [ ] Login with credentials
- [ ] JWT token received and stored
- [ ] Logout clears token
- [ ] Unauthenticated access redirects to login

**How to verify:**
1. Go to http://localhost:8080
2. Click "Register here"
3. Fill form with valid data
4. After registration, redirected to dashboard
5. Logout button works
6. Refresh page → redirects to login

#### 2. Profile Management ✅
- [ ] View own profile with all fields
- [ ] Edit profile (name, phone, bio, skills)
- [ ] Changes persist after save
- [ ] Profile tab shows updated data

**How to verify:**
1. Login
2. Click "Edit Profile" button
3. Update fields and save
4. Reload page → changes visible

#### 3. Coworkers List ✅
- [ ] See employees from same department
- [ ] Sensitive data filtered (coworkers' phone/DOB hidden)
- [ ] Own sensitive data visible
- [ ] Search by name/email works

**How to verify:**
1. Go to "Coworkers" tab
2. Check if phone/DOB missing for coworkers
3. Go to "My Profile" → your phone/DOB visible
4. Use search bar to filter

#### 4. Peer Feedback System ✅
- [ ] Give feedback button opens modal
- [ ] Managers NOT in dropdown (employees can't give feedback to managers)
- [ ] Submit feedback with rating and category
- [ ] AI polish option available (may not work without API key)
- [ ] View given feedbacks
- [ ] Managers see all department feedbacks

**How to verify as Employee:**
1. Go to "Feedback" tab
2. Click "Give Feedback"
3. Dropdown should NOT show any managers
4. Select coworker, rate, write feedback, submit
5. Feedback appears in list

**How to verify as Manager:**
1. Login as manager
2. Go to "Feedback" tab
3. See stats cards and view toggle
4. "All Department Feedback" shows all feedback
5. "My Feedback" shows only feedback given by you

#### 5. Leave Request & Approval ✅
- [ ] Request leave form validates dates
- [ ] Select leave type (vacation, sick, etc.)
- [ ] Submitted requests show in "My Leave Requests"
- [ ] Status badge shows (PENDING/APPROVED/REJECTED)
- [ ] Cancel pending request works

**How to verify as Employee:**
1. Go to "Leave Requests" tab
2. Click "Request Leave"
3. Select type, dates (today or future), reason
4. Submit → appears in list with PENDING status
5. Click "Cancel Request" → status changes to CANCELLED

**How to verify as Manager:**
1. Login as manager
2. Go to "Leave Requests" tab
3. See "Pending Approvals" section at top
4. See badge with count of pending requests
5. Click "Approve" or "Reject" on a request
6. Status updates immediately
7. Employee sees updated status

## 🔒 Security Verification

### Access Control to Test

#### Department-Scoped Access
- [ ] Employees only see coworkers from their department
- [ ] Managers only approve leave for their department
- [ ] Feedback scoped to department

**How to test:**
1. Register 2 users in different departments
2. Login as User A (Engineering)
3. Coworkers tab → should NOT show User B (HR)
4. Login as Manager in Engineering
5. Leave approvals → should NOT show HR requests

#### Sensitive Data Filtering
- [ ] Own profile shows: phone, dateOfBirth, hireDate
- [ ] Coworker profiles show: email, position, department
- [ ] Coworker profiles DON'T show: phone, dateOfBirth, hireDate

**How to test:**
1. Login and view your profile
2. Note: phone, DOB, hire date visible
3. Go to Coworkers tab
4. Check coworker cards → sensitive fields missing

#### Role-Based Features
- [ ] Manager sees feedback stats and toggle
- [ ] Manager sees pending leave approvals
- [ ] Employee doesn't see manager-only controls
- [ ] Employee can't give feedback to managers

**How to test:**
1. Login as employee → no stats cards, no pending approvals section
2. Login as manager → stats cards visible, pending approvals visible

## 📊 Code Quality Verification

### Check GitHub Actions

1. Go to **Actions** tab
2. Latest workflow run should show:
   - ✅ Test (unit + integration)
   - ✅ Build (JAR artifact)
   - ✅ Code Quality (Checkstyle, SpotBugs)
   - ✅ Docker (image build)
   - ✅ Security Scan (Trivy, OWASP)

### Check Test Coverage

1. Click on latest workflow run
2. Scroll to "Test" job
3. View coverage percentage (should be 60%+)
4. Download "test-results" artifact to see detailed report

### Check Security Report

1. Go to **Security** tab
2. Click **Code scanning alerts**
3. Should see Trivy results (if any vulnerabilities found)

## 🐛 Common Issues & Solutions

### Issue: "Failed to connect to database"
**Solution:**
```bash
docker-compose up -d postgres mongodb redis
# Wait 30 seconds
docker-compose ps  # Verify all healthy
```

### Issue: "Port 8080 already in use"
**Solution:**
```bash
# Find process using port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                # macOS/Linux

# Kill process or change port
SERVER_PORT=8081 mvn spring-boot:run
```

### Issue: "JWT token invalid"
**Solution:**
- Clear browser localStorage
- Re-login
- Check application.yml JWT secret is set

### Issue: "Migration checksum mismatch"
**Solution:**
```bash
# Drop and recreate database
docker-compose down -v
docker-compose up -d
# Wait 30 seconds
mvn spring-boot:run
```

### Issue: "Tests fail locally but pass in CI"
**Solution:**
```bash
# Ensure test databases running
docker-compose up -d postgres mongodb redis

# Clean and re-run
mvn clean verify

# Check environment variables match CI
```

## 📝 What to Review

### Code Quality
- [ ] Clean code with meaningful names
- [ ] Proper exception handling
- [ ] Input validation on all endpoints
- [ ] Service layer business logic
- [ ] Repository queries optimized
- [ ] No hardcoded secrets (check .env.example)

### Architecture
- [ ] Layered architecture (Controller → Service → Repository)
- [ ] DTOs for request/response
- [ ] MapStruct for object mapping
- [ ] Proper dependency injection
- [ ] Separation of concerns

### Security
- [ ] JWT authentication on all endpoints (except public)
- [ ] Password hashing with BCrypt
- [ ] Role-based authorization (@PreAuthorize)
- [ ] Service-layer access control
- [ ] Input validation (Jakarta Validation)
- [ ] No SQL injection vulnerabilities

### Testing
- [ ] Integration tests for critical paths
- [ ] Test coverage 60%+
- [ ] CI/CD pipeline configured
- [ ] All tests passing
- [ ] No flaky tests

### Documentation
- [ ] README clear and comprehensive
- [ ] API documented (Swagger/OpenAPI)
- [ ] Architecture documented
- [ ] Setup instructions accurate
- [ ] Security recommendations provided

## ✅ Approval Checklist

Before approving, verify:
- [ ] All CI/CD checks pass
- [ ] Application starts successfully
- [ ] Core features work end-to-end
- [ ] Security measures in place
- [ ] Code quality acceptable
- [ ] Documentation complete
- [ ] No critical vulnerabilities
- [ ] Tests provide good coverage

## 🎯 Expected Outcomes

### After Full Review, You Should See:

1. **Working Application**
   - Employees can register, login, manage profiles
   - Coworkers list shows department colleagues
   - Feedback system enables peer reviews
   - Leave request approval workflow functions

2. **Security Measures**
   - JWT authentication required
   - Role-based access control enforced
   - Sensitive data properly filtered
   - Department-scoped operations

3. **Code Quality**
   - Clean, maintainable codebase
   - Proper architecture and patterns
   - Good test coverage
   - Comprehensive documentation

4. **Production Readiness**
   - Docker containerization
   - Database migrations
   - Environment-based configuration
   - CI/CD pipeline
   - Security scanning
   - Performance considerations documented

## 📞 Questions?

If you have questions or find issues:
1. Check documentation files first
2. Review GitHub Actions logs
3. Check `SECURITY_PERFORMANCE_RECOMMENDATIONS.md` for known limitations
4. Create GitHub issue with details

## 🎉 Success Criteria

**This project is ready for approval if:**
- ✅ CI/CD pipeline passes
- ✅ Application runs without errors
- ✅ Core features demonstrable
- ✅ Security controls validated
- ✅ Code quality acceptable
- ✅ Documentation complete

---

**Time to Review:** 30-60 minutes
**Quick Test:** 10 minutes (just run Docker and test features)
**Full Review:** 2-3 hours (code review + testing + security check)

**Thank you for reviewing!** 🙏
