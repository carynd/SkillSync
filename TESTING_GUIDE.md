# SkillSync Testing Guide

## Quick Status Check

### 1. Check if Services are Running

```bash
# Check AI Service (Python)
curl http://localhost:8001/health

# Check Spring Boot API
curl http://localhost:8080/actuator/health || echo "Spring Boot needs authentication"
```

### 2. Check Service Ports

```bash
# See what's running on port 8001 (AI Service)
lsof -i :8001

# See what's running on port 8080 (Spring Boot)
lsof -i :8080
```

---

## Testing AI Service (Port 8001)

### Test 1: Health Check ✅
```bash
curl http://localhost:8001/health
```

**Expected Response:**
```json
{
    "status": "healthy",
    "service": "SkillSync AI Insights",
    "version": "1.0.0",
    "openai_configured": false
}
```

### Test 2: AI Career Advice (Mock Mode) ✅
```bash
curl -X POST http://localhost:8001/api/ai/advice \
  -H "Content-Type: application/json" \
  -d '{
    "user_profile": {
      "user_id": "test-123",
      "name": "John Doe",
      "current_role": "Junior Developer",
      "target_role": "Senior Full Stack Developer",
      "current_skills": ["JavaScript", "React"],
      "experience_years": 2
    },
    "skill_gap": {
      "skill_gap_percentage": 60.0,
      "alignment_score": 40.0,
      "missing_skills": ["Node.js", "TypeScript", "Docker"],
      "matching_skills_count": 4,
      "missing_skills_count": 3
    },
    "question": "What should I focus on first?"
  }'
```

**Expected Response:**
```json
{
    "advice": "Based on your goal to become a Senior Full Stack Developer...",
    "reasoning": "With 2 years of experience...",
    "action_items": [
        "Start with Node.js - highest priority skill",
        "Dedicate 10-15 hours per week to learning",
        ...
    ],
    "estimated_timeline": "3-6 months with consistent effort",
    "confidence_score": 0.75
}
```

### Test 3: Interactive API Documentation
Open in browser:
- **Swagger UI**: http://localhost:8001/docs
- Try out endpoints interactively!

---

## Testing Spring Boot API (Port 8080)

### Test 1: Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "testuser@example.com",
    "password": "SecurePass123"
  }'
```

**Expected Response:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "testuser@example.com",
    "name": "Test User"
}
```

### Test 2: Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testuser@example.com",
    "password": "SecurePass123"
  }'
```

Save the JWT token from the response!

### Test 3: Get Recommendations (Requires JWT)
```bash
# Replace <YOUR_JWT_TOKEN> with actual token from login
curl http://localhost:8080/api/recommendations/<USER_ID> \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>"
```

### Test 4: Get AI Career Advice (Requires JWT)
```bash
# Replace <YOUR_JWT_TOKEN> and <USER_ID>
curl -X POST http://localhost:8080/api/ai/advice/<USER_ID> \
  -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "How can I transition to a Senior Full Stack Developer role?"
  }'
```

---

## End-to-End Integration Test

This tests the complete flow: User → Spring Boot → AI Service → Response

### Step 1: Create User and Get Token
```bash
# Register
RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Developer",
    "email": "jane@dev.com",
    "password": "DevPass456",
    "currentRole": "Junior Developer",
    "targetRole": "Senior Full Stack Developer",
    "skills": ["JavaScript", "React", "HTML", "CSS"]
  }')

# Extract token and userId
TOKEN=$(echo $RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['token'])")
USER_ID=$(echo $RESPONSE | python3 -c "import sys, json; print(json.load(sys.stdin)['userId'])")

echo "Token: $TOKEN"
echo "User ID: $USER_ID"
```

### Step 2: Get Skill Recommendations
```bash
curl http://localhost:8080/api/recommendations/$USER_ID \
  -H "Authorization: Bearer $TOKEN"
```

### Step 3: Get AI Career Advice
```bash
curl -X POST http://localhost:8080/api/ai/advice/$USER_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "question": "What should I learn first to become a Senior Full Stack Developer?"
  }'
```

**Expected Flow:**
1. Spring Boot receives request
2. Fetches user profile from database
3. Gets skill recommendations from Recommendation Engine
4. Calls AI Service (Python) with combined data
5. AI Service returns personalized advice
6. Spring Boot returns advice to user

---

## Swagger/OpenAPI Documentation

### AI Service (Python FastAPI)
- **URL**: http://localhost:8001/docs
- **Interactive**: Yes, try endpoints directly!

### Spring Boot API
- **URL**: http://localhost:8080/swagger-ui.html
- **Interactive**: Yes, with JWT authentication

---

## Troubleshooting

### AI Service Not Responding
```bash
# Check if Python service is running
lsof -i :8001

# Restart AI service
cd backend/ai-service
python3 run.py
```

### Spring Boot Not Responding
```bash
# Check if running
lsof -i :8080

# Restart Spring Boot
cd backend/skillsync-api
mvn spring-boot:run
```

### Database Connection Issues
```bash
# Check PostgreSQL
docker ps | grep postgres

# Start if not running
docker start skillsync-postgres
```

### Redis Connection Issues
```bash
# Check Redis
docker ps | grep redis

# Start if not running
docker start skillsync-redis
```

---

## What's Working

✅ **AI Service (Python FastAPI)**
- Health checks
- Mock career advice generation
- Structured responses with action items
- Running on http://localhost:8001

✅ **Spring Boot API**
- User authentication (register/login)
- JWT token generation
- Skill recommendations
- AI service integration
- Running on http://localhost:8080

✅ **Integration**
- Spring Boot → AI Service communication via WebClient
- Reactive endpoints with Mono/Flux
- Proper error handling
- CORS configured

---

## Quick Verification Commands

```bash
# 1. AI Service Health
curl http://localhost:8001/health

# 2. AI Service Working
curl -X POST http://localhost:8001/api/ai/advice \
  -H "Content-Type: application/json" \
  -d @backend/ai-service/test_request.json

# 3. Spring Boot Running
curl http://localhost:8080/swagger-ui.html | head -5

# 4. Services Connected
# (This requires a valid JWT token and user ID)
```

---

## Next Steps

1. **Add Real OpenAI Integration**: Set OPENAI_API_KEY in `.env` file
2. **Build Frontend**: React dashboard to visualize recommendations
3. **Deploy**: Configure for production deployment
4. **Monitoring**: Add logging and monitoring tools
