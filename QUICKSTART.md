# SkillSync - Quick Start Guide

Get SkillSync up and running in minutes!

## Option 1: Using Docker (Recommended)

### Prerequisites
- Docker and Docker Compose installed

### Steps

1. **Start the databases**:
```bash
docker-compose up -d
```

This will start PostgreSQL and Redis in Docker containers.

2. **Run the backend**:
```bash
cd backend/skillsync-api
mvn spring-boot:run
```

3. **Access the application**:
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Option 2: Local Installation

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 14+
- Redis 6+

### Steps

1. **Install PostgreSQL**:
```bash
# macOS
brew install postgresql
brew services start postgresql

# Create database
createdb skillsync
```

2. **Install Redis**:
```bash
# macOS
brew install redis
brew services start redis
```

3. **Configure environment variables**:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=skillsync
export DB_USERNAME=postgres
export DB_PASSWORD=postgres
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

4. **Run the application**:
```bash
cd backend/skillsync-api
mvn clean install
mvn spring-boot:run
```

## Testing the API

### 1. Register a new user:
```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "password123",
    "currentRole": "Software Developer",
    "targetRole": "Senior Backend Engineer",
    "skills": ["Java", "Spring Boot", "PostgreSQL"],
    "experience": 3,
    "goals": "Become a senior backend engineer with expertise in microservices"
  }'
```

### 2. Login:
```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

Save the JWT token from the response!

### 3. Get user profile:
```bash
curl -X GET http://localhost:8080/api/users/{userId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Update user profile:
```bash
curl -X PUT http://localhost:8080/api/users/{userId} \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Redis"]
  }'
```

## Using Swagger UI

1. Open http://localhost:8080/swagger-ui.html in your browser
2. Test all endpoints interactively
3. For authenticated endpoints:
   - Click "Authorize" button
   - Enter: `Bearer YOUR_JWT_TOKEN`
   - Click "Authorize"

## Next Steps

1. Explore the API documentation in Swagger UI
2. Check out the [main README](README.md) for architecture details
3. Review the [project specification](🧭%20Project%20Specification.ini) for planned features

## Troubleshooting

### Database connection failed
- Check if PostgreSQL is running: `pg_isready`
- Verify database exists: `psql -l | grep skillsync`
- Check credentials in application.yml

### Redis connection failed
- Check if Redis is running: `redis-cli ping`
- Should return: `PONG`

### Port already in use
- Change the port in application.yml:
  ```yaml
  server:
    port: 8081
  ```

### Build errors
- Ensure Java 17 is installed: `java -version`
- Clean and rebuild: `mvn clean install`

## Stop the Application

### Stop Docker containers:
```bash
docker-compose down
```

### Stop local services:
```bash
# PostgreSQL
brew services stop postgresql

# Redis
brew services stop redis
```

---

Happy coding! 🚀
