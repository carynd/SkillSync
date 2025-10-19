# SkillSync API Documentation

## Base URL

**Development**: `http://localhost:8080/api`
**Production**: TBD

## Authentication

Most endpoints require JWT authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

---

## API Endpoints

### Authentication & User Management

#### 1. Register New User

**Endpoint**: `POST /users/register`

**Request Body**:
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword123",
  "currentRole": "Frontend Engineer",
  "targetRole": "Full Stack Engineer",
  "skills": ["React", "JavaScript", "TypeScript"],
  "experienceLevel": "INTERMEDIATE",
  "goals": "Become a full stack developer",
  "githubUsername": "johndoe"
}
```

**Experience Level Enum** (Required):
- `BEGINNER` - 0-2 years of experience
- `INTERMEDIATE` - 3-5 years of experience
- `ADVANCED` - 6-10 years of experience
- `EXPERT` - 10+ years of experience

**Response**: `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "name": "John Doe"
}
```

#### 2. Login User

**Endpoint**: `POST /users/login`

**Request Body**:
```json
{
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Response**: `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "name": "John Doe"
}
```

#### 3. Get User Profile

**Endpoint**: `GET /users/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Response**: `200 OK`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "name": "John Doe",
  "email": "john@example.com",
  "currentRole": "Frontend Engineer",
  "targetRole": "Full Stack Engineer",
  "skills": ["React", "JavaScript", "TypeScript"],
  "experienceLevel": "INTERMEDIATE",
  "goals": "Become a full stack developer",
  "githubUsername": "johndoe",
  "createdAt": "2025-10-01T10:00:00",
  "updatedAt": "2025-10-15T14:30:00"
}
```

#### 4. Update User Profile

**Endpoint**: `PUT /users/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Request Body** (all fields optional):
```json
{
  "name": "John Doe",
  "email": "newemail@example.com",
  "currentRole": "Full Stack Engineer",
  "targetRole": "Senior Full Stack Engineer",
  "skills": ["React", "Node.js", "PostgreSQL", "Docker"],
  "experienceLevel": "ADVANCED",
  "goals": "Lead engineering teams",
  "githubUsername": "johndoe"
}
```

**Response**: `200 OK` (same structure as Get User Profile)

#### 5. Get Available Skills

**Endpoint**: `GET /users/skills`

**Description**: Returns a predefined list of 150+ industry-standard skills for consistent data entry.

**Response**: `200 OK`
```json
[
  "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust",
  "React", "Angular", "Vue.js", "Next.js", "Redux",
  "Spring Boot", "Node.js", "Django", "Express", "FastAPI",
  "PostgreSQL", "MongoDB", "MySQL", "Redis", "Cassandra",
  "Docker", "Kubernetes", "Jenkins", "GitHub Actions",
  "AWS", "Azure", "GCP", "Terraform",
  "... 150+ more skills"
]
```

---

### Job Intelligence

#### 6. Get Available Roles

**Endpoint**: `GET /jobs/roles`

**Headers**: `Authorization: Bearer <token>`

**Description**: Returns list of available job roles with predefined skill data.

**Response**: `200 OK`
```json
[
  "Backend Engineer",
  "Frontend Engineer",
  "Full Stack Engineer",
  "Data Scientist",
  "DevOps Engineer",
  "Product Manager"
]
```

#### 7. Sync Job Data

**Endpoint**: `POST /jobs/sync`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "role": "Backend Engineer",
  "location": "United States",
  "maxResults": 100
}
```

**Description**: Syncs job market data for a specific role. In dev mode, uses mock data. In production, would fetch from real APIs (LinkedIn, GitHub Jobs, etc.).

**Response**: `200 OK`
```json
{
  "role": "Backend Engineer",
  "totalJobsAnalyzed": 100,
  "source": "Mock Data (Development)",
  "skills": [
    {
      "skillName": "Java",
      "demandScore": 95,
      "category": "Programming Language",
      "frequency": 95.0
    },
    {
      "skillName": "Spring Boot",
      "demandScore": 88,
      "category": "Framework",
      "frequency": 88.0
    }
    // ... more skills
  ]
}
```

#### 8. Get Skills for Role

**Endpoint**: `GET /jobs/{role}/skills`

**Headers**: `Authorization: Bearer <token>`

**Path Parameters**:
- `role` - Job role name (URL encoded if contains spaces)

**Response**: `200 OK`
```json
[
  {
    "id": 1,
    "role": "Backend Engineer",
    "skill": "Java",
    "demandScore": 95,
    "category": "Programming Language",
    "source": "Mock Data",
    "createdAt": "2025-10-01T10:00:00",
    "updatedAt": "2025-10-15T14:30:00"
  }
  // ... more skills
]
```

---

### Recommendations

#### 9. Get Recommendations

**Endpoint**: `GET /recommendations/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Description**: Retrieves cached recommendations for a user. Returns 404 if no recommendations exist (trigger generation with POST).

**Response**: `200 OK`
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "userName": "John Doe",
  "currentRole": "Frontend Engineer",
  "targetRole": "Full Stack Engineer",
  "currentSkills": ["React", "JavaScript", "TypeScript"],
  "skillGapPercentage": 40.0,
  "alignmentScore": 60.0,
  "missingSkillsCount": 4,
  "matchingSkillsCount": 6,
  "recommendations": [
    {
      "skillName": "Node.js",
      "category": "Runtime",
      "demandScore": 85,
      "priority": "High",
      "reason": "Essential skill with 85% market demand. Highly sought after by employers.",
      "estimatedLearningTime": "4-6 weeks"
    },
    {
      "skillName": "PostgreSQL",
      "category": "Database",
      "demandScore": 80,
      "priority": "High",
      "reason": "Essential skill with 80% market demand. Highly sought after by employers.",
      "estimatedLearningTime": "3-4 weeks"
    }
    // ... more recommendations
  ],
  "generatedAt": "2025-10-18T10:30:00"
}
```

**Priority Levels**:
- `High` - Demand score >= 80
- `Medium` - Demand score >= 60
- `Low` - Demand score < 60

**Error Responses**:
- `404 Not Found` - No cached recommendations (call POST to generate)
- `400 Bad Request` - User hasn't set target role

#### 10. Generate Recommendations

**Endpoint**: `POST /recommendations/generate/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Description**: Generates new personalized recommendations based on user's profile and target role. Calculates skill gaps, prioritizes learning paths, and caches results for 24 hours.

**Response**: `200 OK` (same structure as GET /recommendations)

**Process**:
1. Fetches user profile (target role, current skills)
2. Retrieves skill demand data for target role from database
3. Calculates skill gap (missing vs matching skills)
4. Prioritizes recommendations by demand score
5. Estimates learning time based on skill category
6. Caches results with 24-hour expiry

**Error Responses**:
- `400 Bad Request` - User must set target role first
- `500 Internal Server Error` - No skill data available for target role

#### 11. Clear Recommendations

**Endpoint**: `DELETE /recommendations/{userId}`

**Headers**: `Authorization: Bearer <token>`

**Description**: Clears cached recommendations for a user. Useful when user updates their profile significantly.

**Response**: `204 No Content`

---

### AI Insights

#### 12. Get Career Advice

**Endpoint**: `POST /ai/advice`

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "question": "How can I transition from Frontend to Full Stack development?"
}
```

**Description**: Uses AI (GPT-4) to provide personalized career advice based on user's profile, skill gaps, and specific question.

**Response**: `200 OK`
```json
{
  "advice": "To transition from Frontend to Full Stack development, I recommend focusing on backend technologies that complement your existing JavaScript knowledge. Start with Node.js and Express since you're already familiar with JavaScript. This will make the learning curve gentler...",
  "reasoning": "Given your strong foundation in React and JavaScript, Node.js is the most natural next step because it allows you to leverage your existing JavaScript skills while learning server-side concepts. PostgreSQL is recommended because it's widely used and has excellent documentation...",
  "actionItems": [
    "Complete a Node.js and Express fundamentals course",
    "Build a REST API project connecting to PostgreSQL",
    "Learn database design and SQL basics",
    "Create a full-stack project combining React and Node.js"
  ],
  "estimatedTimeline": "3-4 months with consistent effort",
  "confidenceScore": 0.85,
  "generatedAt": "2025-10-18T10:45:00"
}
```

**Features**:
- Context-aware (considers user's current skills, experience level, target role)
- Personalized action items
- Realistic timelines
- Falls back to smart mock responses if OpenAI not configured

#### 13. AI Service Health Check

**Endpoint**: `GET /ai/health`

**Response**: `200 OK`
```json
{
  "status": "healthy",
  "service": "SkillSync AI Insights",
  "version": "1.0.0",
  "openai_configured": true
}
```

---

## Data Models

### ExperienceLevel Enum

```java
public enum ExperienceLevel {
    BEGINNER("0-2 years"),
    INTERMEDIATE("3-5 years"),
    ADVANCED("6-10 years"),
    EXPERT("10+ years");
}
```

**Usage in Requests**:
- User Registration: `"experienceLevel": "INTERMEDIATE"`
- User Update: `"experienceLevel": "ADVANCED"`

**Database Storage**: Stored as VARCHAR with CHECK constraint

**Frontend Display**:
```javascript
{
  value: 'INTERMEDIATE',
  label: '3-5 years (Intermediate)'
}
```

### Skills List

**Total Skills**: 150+

**Categories**:
- Programming Languages (25+)
- Frontend Frameworks (15+)
- Backend Frameworks (15+)
- Databases (15+)
- DevOps & Cloud (20+)
- Testing & Tools (15+)
- Mobile Development (10+)
- Machine Learning (15+)
- Soft Skills (10+)

**Access**: `GET /api/users/skills`

**Usage**: Multi-select dropdown in Profile page

---

## Error Responses

### Standard Error Format

```json
{
  "timestamp": "2025-10-18T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "User must set a target role first",
  "path": "/api/recommendations/generate/550e8400-e29b-41d4-a716-446655440000"
}
```

### Common HTTP Status Codes

- `200 OK` - Request successful
- `201 Created` - Resource created
- `204 No Content` - Request successful, no body returned
- `400 Bad Request` - Invalid request data
- `401 Unauthorized` - Missing or invalid JWT token
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

---

## Rate Limiting

**Development**: No rate limiting
**Production**: TBD

---

## Pagination

**Not yet implemented**. All list endpoints return full results.

**Planned** for:
- Job search results
- Recommendations history
- User activity logs

---

## Versioning

**Current Version**: v1 (implicit in `/api` prefix)

**Future**: Will use `/api/v2` for breaking changes

---

## Testing Endpoints

### Using cURL

```bash
# Register
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123",
    "targetRole": "Backend Engineer",
    "skills": ["Java", "Spring Boot"],
    "experienceLevel": "INTERMEDIATE"
  }'

# Login
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Get User (with token)
curl http://localhost:8080/api/users/{userId} \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"

# Generate Recommendations
curl -X POST http://localhost:8080/api/recommendations/generate/{userId} \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### Using Postman

1. Import collection (TBD)
2. Set environment variable `baseUrl` to `http://localhost:8080/api`
3. Set environment variable `token` after login
4. Use `{{baseUrl}}` and `{{token}}` in requests

---

## OpenAPI/Swagger Documentation

**URL**: `http://localhost:8080/swagger-ui.html` (when running)

**Features**:
- Interactive API testing
- Request/response examples
- Schema documentation
- Try-it-out functionality

---

**API Version**: 1.0
**Last Updated**: October 2025
**Base URL**: http://localhost:8080/api
