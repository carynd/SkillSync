# How to Run SkillSync - Complete Guide

## ✅ What We've Set Up

Your SkillSync backend is now fully installed and running! Here's what's working:

- ✅ PostgreSQL database (running)
- ✅ Redis cache (running)
- ✅ Spring Boot backend (running on port 8080)
- ✅ Swagger API documentation
- ✅ JWT authentication system
- ✅ User management APIs

## 🚀 Quick Start

### Start Everything (Next Time)

When you come back to work on the project, run these commands:

```bash
# 1. Start PostgreSQL and Redis (if not already running)
brew services start postgresql@15
brew services start redis

# 2. Navigate to the project
cd ~/Desktop/Academics/2025/Project/SkilSync/backend/skillsync-api

# 3. Set Java 17 environment variables
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:/opt/homebrew/opt/postgresql@15/bin:$PATH"

# 4. Run the application
mvn spring-boot:run
```

The application will start on **http://localhost:8080**

## 📊 Access Points

### 1. Swagger UI (API Documentation & Testing)
**URL**: http://localhost:8080/swagger-ui/index.html

This is your interactive API documentation where you can:
- See all available endpoints
- Test APIs directly in your browser
- View request/response schemas

### 2. API Endpoints

Base URL: `http://localhost:8080`

**User Management APIs:**
- `POST /api/users/register` - Register a new user
- `POST /api/users/login` - Login and get JWT token
- `GET /api/users/{userId}` - Get user profile (requires JWT)
- `PUT /api/users/{userId}` - Update user profile (requires JWT)

## 🧪 Testing the APIs

### 1. Register a New User

Open your terminal and run:

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
    "goals": "Become a senior backend engineer"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "name": "John Doe"
}
```

**💾 Save this token!** You'll need it for authenticated requests.

### 2. Login

```bash
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "password123"
  }'
```

### 3. Get User Profile (with JWT token)

Replace `YOUR_USER_ID` and `YOUR_JWT_TOKEN` with actual values:

```bash
curl -X GET "http://localhost:8080/api/users/YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Update User Profile

```bash
curl -X PUT "http://localhost:8080/api/users/YOUR_USER_ID" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Redis", "AWS"]
  }'
```

## 🔍 Checking What's Running

### Check if PostgreSQL is running:
```bash
brew services list | grep postgresql
```

Should show: `postgresql@15  started`

### Check if Redis is running:
```bash
brew services list | grep redis
```

Should show: `redis  started`

### Test Redis connection:
```bash
redis-cli ping
```

Should return: `PONG`

### Check if Spring Boot is running:
```bash
curl -s http://localhost:8080/swagger-ui/index.html | head -5
```

Should return HTML content.

## 🛑 Stopping Everything

### Stop the Spring Boot application:
Press `Ctrl+C` in the terminal where mvn is running

### Stop PostgreSQL and Redis:
```bash
brew services stop postgresql@15
brew services stop redis
```

## 🔧 Troubleshooting

### Port 8080 Already in Use
```bash
# Find what's using port 8080
lsof -i :8080

# Kill the process (replace PID with actual number)
kill -9 PID
```

### Database Connection Error
```bash
# Restart PostgreSQL
brew services restart postgresql@15

# Check if database exists
psql -l | grep skillsync
```

### Redis Connection Error
```bash
# Restart Redis
brew services restart redis

# Test connection
redis-cli ping
```

### "Java version mismatch" Error
Make sure you're using Java 17:
```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
```

Should show: `openjdk version "17.0.16"`

## 📁 Database Location

Your PostgreSQL data is stored at:
```
/opt/homebrew/var/postgresql@15
```

Your Redis data is stored at:
```
/opt/homebrew/var/db/redis
```

## 🗄️ Viewing the Database

### Connect to PostgreSQL:
```bash
psql skillsync
```

### Useful PostgreSQL commands:
```sql
-- List all tables
\dt

-- View users table
SELECT * FROM users;

-- View skill demand data
SELECT * FROM skill_demand;

-- View recommendations
SELECT * FROM recommendations;

-- Exit
\q
```

## 📊 What's in the Database

After you register a user, the database will have:

**Tables created automatically:**
- `users` - User profiles and authentication
- `skill_demand` - Job market skill data (empty for now)
- `recommendations` - Cached recommendations (empty for now)

## 🎨 Using Swagger UI

1. Open http://localhost:8080/swagger-ui/index.html in your browser
2. You'll see all available APIs organized by controller
3. Click on any endpoint to expand it
4. Click "Try it out" button
5. Fill in the request body/parameters
6. Click "Execute"
7. View the response below

**For authenticated endpoints:**
1. First, register or login to get a JWT token
2. Click the "Authorize" button at the top of Swagger UI
3. Enter: `Bearer YOUR_JWT_TOKEN`
4. Click "Authorize"
5. Now you can test protected endpoints

## ⚙️ Environment Variables

The application uses these environment variables (with defaults):

```bash
# Database
DB_HOST=localhost          # PostgreSQL host
DB_PORT=5432              # PostgreSQL port
DB_NAME=skillsync         # Database name
DB_USERNAME=postgres      # Database user
DB_PASSWORD=postgres      # Database password

# Redis
REDIS_HOST=localhost      # Redis host
REDIS_PORT=6379          # Redis port

# Server
SERVER_PORT=8080         # Application port

# JWT
JWT_SECRET=your-secret-key-change-this-in-production-min-256-bits-long
JWT_EXPIRATION=86400000  # 24 hours
```

To customize, export them before running:
```bash
export DB_PASSWORD=mypassword
export SERVER_PORT=9090
mvn spring-boot:run
```

## 📝 Next Steps

Now that your backend is running, you can:

1. **Test all the APIs** using Swagger UI or curl
2. **Build the Job Intelligence Service** to fetch real job market data
3. **Create the Recommendation Engine** for skill gap analysis
4. **Add the AI Insights microservice** with Python FastAPI
5. **Build the React frontend** dashboard
6. **Deploy to AWS** with Docker and CI/CD

## 💡 Tips

- **Keep the terminal open** where mvn spring-boot:run is running to see logs
- **Check logs** if something goes wrong - they're very detailed
- **Use Swagger UI** for quick testing - it's faster than curl
- **PostgreSQL and Redis** will auto-start on system reboot (because we used `brew services start`)

## 🆘 Need Help?

If something isn't working:

1. Check the Spring Boot logs in the terminal
2. Verify PostgreSQL and Redis are running
3. Make sure you're using Java 17
4. Check that port 8080 is not in use
5. Look at the error messages - they're usually very specific

---

## 🎉 Congratulations!

Your SkillSync backend is fully operational! You now have:
- ✅ A production-ready Spring Boot API
- ✅ Secure JWT authentication
- ✅ PostgreSQL database with proper schema
- ✅ Redis caching layer
- ✅ Interactive API documentation
- ✅ Complete user management system

**You're ready to build amazing features!** 🚀

