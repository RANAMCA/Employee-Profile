# Database Debugging Guide

Quick reference for accessing and debugging databases in Docker containers.

## PostgreSQL Commands

### Quick Data Inspection
```bash
# View all employees
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT id, first_name, last_name, email, role FROM employees;"

# View all absences
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT * FROM absences;"

# Count records
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT COUNT(*) FROM employees;"

# View table structure
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "\d employees"
```

### Interactive PostgreSQL Shell
```bash
# Connect to interactive shell
docker exec -it employee-profile-postgres psql -U postgres -d employee_profile

# Useful commands inside psql:
\dt                    # List all tables
\d table_name          # Describe table structure
\l                     # List all databases
\du                    # List users
\q                     # Quit
```

### Connection from Host
```bash
# Docker PostgreSQL runs on port 5433 to avoid conflicts with local PostgreSQL
psql -h localhost -p 5433 -U postgres -d employee_profile
# When prompted, just press Enter (trust authentication)

# Or use Docker exec (no password needed)
docker exec -it employee-profile-postgres psql -U postgres -d employee_profile
```

---

## MongoDB Commands

### Quick Data Inspection
```bash
# View all feedbacks
docker exec employee-profile-mongodb mongosh employee_profile --quiet --eval "db.feedbacks.find().pretty()"

# Count documents
docker exec employee-profile-mongodb mongosh employee_profile --quiet --eval "db.feedbacks.countDocuments()"

# List all collections
docker exec employee-profile-mongodb mongosh employee_profile --quiet --eval "show collections"
```

### Interactive MongoDB Shell
```bash
# Connect to interactive shell (Windows - use winpty prefix if needed)
docker exec -it employee-profile-mongodb mongosh employee_profile

# Useful commands inside mongosh:
show collections       # List collections
db.feedbacks.find()    # View all feedbacks
db.feedbacks.find().pretty()  # Pretty print
db.feedbacks.countDocuments()
exit                   # Quit
```

### Connection from Host
```bash
mongosh mongodb://localhost:27017/employee_profile
```

---

## Redis Commands

### Quick Data Inspection
```bash
# List all keys
docker exec employee-profile-redis redis-cli KEYS "*"

# Get value of a key
docker exec employee-profile-redis redis-cli GET "key-name"

# Get Redis info
docker exec employee-profile-redis redis-cli INFO

# Check memory usage
docker exec employee-profile-redis redis-cli INFO memory
```

### Interactive Redis Shell
```bash
# Connect to interactive shell
docker exec -it employee-profile-redis redis-cli

# Useful commands inside redis-cli:
KEYS *                 # List all keys
GET key_name           # Get value
SET key value          # Set value
DEL key                # Delete key
FLUSHALL               # Clear all data (CAREFUL!)
INFO                   # Redis stats
MONITOR                # Watch commands in real-time
QUIT                   # Exit
```

### Connection from Host
```bash
redis-cli -h localhost -p 6379
```

---

## Container Management

### View Logs
```bash
# All logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# Specific container logs
docker logs employee-profile-postgres -f
docker logs employee-profile-mongodb -f
docker logs employee-profile-redis -f
docker logs employee-profile-app -f
```

### Container Status
```bash
# List containers
docker-compose ps

# Resource usage
docker stats

# Inspect container
docker inspect employee-profile-postgres
```

### Execute Shell Commands
```bash
# PostgreSQL container bash
docker exec -it employee-profile-postgres bash

# MongoDB container bash
docker exec -it employee-profile-mongodb bash

# Redis container shell
docker exec -it employee-profile-redis sh

# App container bash
docker exec -it employee-profile-app sh
```

---

## GUI Tools (Recommended)

### PostgreSQL
- **pgAdmin**: https://www.pgadmin.org/
- **DBeaver**: https://dbeaver.io/
- **TablePlus**: https://tableplus.com/

**Connection Details:**
- Host: `localhost`
- Port: `5433` (Docker PostgreSQL uses port 5433 to avoid conflicts)
- Database: `employee_profile`
- Username: `postgres`
- Password: Leave empty or use `postgres`

### MongoDB
- **MongoDB Compass**: https://www.mongodb.com/products/compass
- **Studio 3T**: https://studio3t.com/
- **Robo 3T**: https://robomongo.org/

**Connection Details:**
- URI: `mongodb://localhost:27017/employee_profile`

### Redis
- **RedisInsight**: https://redis.com/redis-enterprise/redis-insight/
- **Another Redis Desktop Manager**: https://github.com/qishibo/AnotherRedisDesktopManager

**Connection Details:**
- Host: `localhost`
- Port: `6379`
- No password

---

## Common Debugging Scenarios

### Check if User Exists
```bash
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT * FROM employees WHERE email='john.manager@newwork.com';"
```

### View Password Hash
```bash
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT email, password FROM employees WHERE id=1;"
```

### Check Absence Status
```bash
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT e.first_name, e.last_name, a.type, a.status FROM absences a JOIN employees e ON a.employee_id = e.id;"
```

### View All Feedback
```bash
docker exec employee-profile-mongodb mongosh employee_profile --quiet --eval "db.feedbacks.find({}, {employeeId: 1, originalContent: 1, isPolished: 1}).pretty()"
```

### Check Redis Cache
```bash
docker exec employee-profile-redis redis-cli KEYS "ai-feedback*"
```

---

## Database Backup & Restore

### PostgreSQL Backup
```bash
# Full database dump
docker exec employee-profile-postgres pg_dump -U postgres employee_profile > backup.sql

# Specific table
docker exec employee-profile-postgres pg_dump -U postgres -t employees employee_profile > employees_backup.sql
```

### PostgreSQL Restore
```bash
docker exec -i employee-profile-postgres psql -U postgres employee_profile < backup.sql
```

### MongoDB Backup
```bash
# Full database dump
docker exec employee-profile-mongodb mongodump --db=employee_profile --out=/tmp/backup

# Copy from container
docker cp employee-profile-mongodb:/tmp/backup ./mongodb_backup
```

### MongoDB Restore
```bash
docker exec employee-profile-mongodb mongorestore --db=employee_profile /tmp/backup/employee_profile
```

---

## Troubleshooting

### Container Won't Start
```bash
# Check logs
docker logs employee-profile-postgres --tail 50

# Check if port is in use
netstat -ano | findstr :5432
netstat -ano | findstr :27017
netstat -ano | findstr :6379
```

### Reset Database
```bash
# Stop and remove volumes
docker-compose down -v

# Restart fresh
docker-compose up --build
```

### Check Database Connections
```bash
# PostgreSQL active connections
docker exec employee-profile-postgres psql -U postgres -d employee_profile -c "SELECT count(*) FROM pg_stat_activity;"

# MongoDB connections
docker exec employee-profile-mongodb mongosh employee_profile --quiet --eval "db.serverStatus().connections"

# Redis connected clients
docker exec employee-profile-redis redis-cli INFO clients
```
