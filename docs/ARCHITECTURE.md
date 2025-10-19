# SkillSync - System Architecture

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                             │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │         React Frontend (Port 3000)                       │   │
│  │  - TailwindCSS  - Chart.js  - Axios                     │   │
│  └─────────────────────────────────────────────────────────┘   │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTPS/REST
┌───────────────────────────┴─────────────────────────────────────┐
│                      API GATEWAY (Future)                        │
└───────────────────────────┬─────────────────────────────────────┘
                            │
        ┌───────────────────┼───────────────────┐
        │                   │                   │
┌───────▼────────┐  ┌──────▼────────┐  ┌──────▼────────┐
│  Spring Boot   │  │  AI Insights  │  │    External   │
│   Backend      │  │  Microservice │  │      APIs     │
│  (Port 8080)   │  │  (Port 8000)  │  │               │
│                │  │               │  │  - RapidAPI   │
│ - User Mgmt    │  │ - FastAPI     │  │  - LinkedIn   │
│ - Job Intel    │  │ - OpenAI      │  │  - GitHub     │
│ - Recommend    │  │               │  │               │
└───────┬────────┘  └───────────────┘  └───────────────┘
        │
        │
┌───────┴────────────────────────────────┐
│         DATA & CACHE LAYER             │
│                                         │
│  ┌──────────────┐  ┌────────────────┐ │
│  │ PostgreSQL   │  │     Redis      │ │
│  │ (Port 5432)  │  │  (Port 6379)   │ │
│  │              │  │                │ │
│  │ - users      │  │ - JWT tokens   │ │
│  │ - skill_     │  │ - Recommend.   │ │
│  │   demand     │  │ - Job data     │ │
│  │ - recommend. │  │                │ │
│  └──────────────┘  └────────────────┘ │
└────────────────────────────────────────┘
```

## Component Architecture

### 1. Backend Service (Spring Boot)

```
┌─────────────────────────────────────────────────┐
│            Spring Boot Application               │
│                                                   │
│  ┌────────────────────────────────────────────┐ │
│  │           Controller Layer                  │ │
│  │  - UserController                          │ │
│  │  - JobController (planned)                 │ │
│  │  - RecommendationController (planned)      │ │
│  └─────────────┬──────────────────────────────┘ │
│                │                                  │
│  ┌─────────────▼──────────────────────────────┐ │
│  │           Service Layer                     │ │
│  │  - UserService      ✅                     │ │
│  │  - JobService       🚧                     │ │
│  │  - RecommendationService 🚧               │ │
│  └─────────────┬──────────────────────────────┘ │
│                │                                  │
│  ┌─────────────▼──────────────────────────────┐ │
│  │         Repository Layer                    │ │
│  │  - UserRepository   ✅                     │ │
│  │  - SkillDemandRepository ✅               │ │
│  │  - RecommendationRepository ✅            │ │
│  └─────────────┬──────────────────────────────┘ │
│                │                                  │
│  ┌─────────────▼──────────────────────────────┐ │
│  │     Cross-Cutting Concerns                  │ │
│  │  - Security (JWT) ✅                       │ │
│  │  - Exception Handling ✅                   │ │
│  │  - Caching ✅                              │ │
│  │  - Validation ✅                           │ │
│  └─────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────┘
```

### 2. Data Model

```
┌──────────────────────────┐
│          User            │
├──────────────────────────┤
│ user_id (PK)             │
│ name                     │
│ email (unique)           │
│ password                 │
│ current_role             │
│ target_role              │
│ skills[]                 │  ← Array (150+ predefined options)
│ experience_level         │  ← ENUM: BEGINNER/INTERMEDIATE/ADVANCED/EXPERT
│ goals                    │
│ github_username          │
│ created_at               │
│ updated_at               │
└────────┬─────────────────┘
         │
         │ 1:N
         │
┌────────▼──────────┐
│  Recommendation   │
├───────────────────┤
│ id (PK)           │
│ user_id (FK)      │
│ missing_skills[]  │
│ resources (JSON)  │
│ skill_gap_%       │
│ alignment_score   │
│ created_at        │
│ expires_at        │
└───────────────────┘

┌───────────────────┐
│   SkillDemand     │
├───────────────────┤
│ id (PK)           │
│ role              │
│ skill             ���
│ demand_score      │
│ source            │
│ category          │
│ created_at        │
│ updated_at        │
└───────────────────┘
```

### 3. Security Flow

```
┌──────────┐                                  ┌──────────────┐
│  Client  │                                  │   Backend    │
└────┬─────┘                                  └──────┬───────┘
     │                                               │
     │ 1. POST /api/users/register                  │
     │   {email, password, ...}                     │
     ├──────────────────────────────────────────────>│
     │                                               │
     │                      2. Hash password         │
     │                      3. Save to DB            │
     │                      4. Generate JWT          │
     │                                               │
     │ 5. Return {token, userId, email, name}       │
     │<──────────────────────────────────────────────┤
     │                                               │
     │ 6. Store token in localStorage                │
     │                                               │
     │ 7. GET /api/users/{userId}                   │
     │    Authorization: Bearer <token>             │
     ├──────────────────────────────────────────────>│
     │                                               │
     │                      8. Validate JWT          │
     │                      9. Extract email         │
     │                      10. Load user            │
     │                                               │
     │ 11. Return user data                          │
     │<──────────────────────────────────────────────┤
     │                                               │
```

### 4. Recommendation Engine Flow (Planned)

```
┌────────┐         ┌──────────┐         ┌───────┐         ┌──────┐
│ Client │         │ Backend  │         │ Redis │         │  DB  │
└───┬────┘         └────┬─────┘         └───┬───┘         └──┬───┘
    │                   │                   │                 │
    │ GET /recommendations/{userId}         │                 │
    ├──────────────────>│                   │                 │
    │                   │                   │                 │
    │                   │ Check cache       │                 │
    │                   ├──────────────────>│                 │
    │                   │                   │                 │
    │                   │ Cache HIT?        │                 │
    │                   │<──────────────────┤                 │
    │                   │                   │                 │
    │                   │ If MISS:          │                 │
    │                   │ Get user skills   │                 │
    │                   ├─────────────────────────────────────>│
    │                   │                   │                 │
    │                   │ Get skill demand for target role    │
    │                   ├─────────────────────────────────────>│
    │                   │                   │                 │
    │                   │ Calculate gap     │                 │
    │                   │ Find resources    │                 │
    │                   │                   │                 │
    │                   │ Cache result (24h)│                 │
    │                   ├──────────────────>│                 │
    │                   │                   │                 │
    │ Return recommendations                │                 │
    │<──────────────────┤                   │                 │
    │                   │                   │                 │
```

### 5. Technology Stack Details

| Layer | Technology | Purpose |
|-------|-----------|---------|
| **API Layer** | Spring Web | REST endpoints |
| **Security** | Spring Security + JWT | Authentication & Authorization |
| **Data Access** | Spring Data JPA | ORM and database operations |
| **Caching** | Spring Data Redis | Performance optimization |
| **Validation** | Jakarta Bean Validation | Input validation |
| **Documentation** | Swagger/OpenAPI | API documentation |
| **Database** | PostgreSQL | Persistent storage |
| **Cache Store** | Redis | In-memory caching |
| **Build Tool** | Maven | Dependency management |
| **Testing** | JUnit + Mockito | Unit & integration tests |

### 6. Deployment Architecture (Planned)

```
┌─────────────────────────────────────────────────────────────┐
│                      AWS Cloud                               │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                  Route 53 (DNS)                       │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                      │
│  ┌────────────────────▼─────────────────────────────────┐  │
│  │         Application Load Balancer                     │  │
│  └────────────────────┬─────────────────────────────────┘  │
│                       │                                      │
│         ┌─────────────┼─────────────┐                       │
│         │             │             │                       │
│  ┌──────▼──────┐ ┌───▼─────┐ ┌────▼──────┐               │
│  │   ECS Task  │ │ECS Task │ │ ECS Task  │               │
│  │  (Backend)  │ │(Backend)│ │ (AI Svc)  │               │
│  └──────┬──────┘ └────┬────┘ └─────┬─────┘               │
│         │             │             │                       │
│         └─────────────┼─────────────┘                       │
│                       │                                      │
│         ┌─────────────┴─────────────┐                       │
│         │                           │                       │
│  ┌──────▼────────┐        ┌─────────▼─────────┐           │
│  │  RDS          │        │  ElastiCache      │           │
│  │  PostgreSQL   │        │  Redis            │           │
│  └───────────────┘        └───────────────────┘           │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### 7. CI/CD Pipeline (Planned)

```
┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│   Git    │────>│  Build   │────>│   Test   │────>│  Deploy  │
│   Push   │     │  Maven   │     │  JUnit   │     │   ECS    │
└──────────┘     └──────────┘     └──────────┘     └──────────┘
     │                │                 │                 │
     │                │                 │                 │
     └────────────────┴─────────────────┴─────────────────┘
              GitHub Actions Workflow
```

## Performance Considerations

### Caching Strategy
- **User sessions**: Redis (JWT blacklist/whitelist)
- **Recommendations**: Redis (24-hour TTL)
- **Job market data**: Redis (updated daily)

### Scalability
- Stateless API design (horizontal scaling ready)
- Connection pooling for database
- Redis cluster support
- Load balancer ready

### Security
- JWT-based stateless authentication
- Password hashing with BCrypt
- CORS configuration for frontend
- Input validation on all endpoints
- SQL injection prevention (JPA)

---

## Recent Updates (October 2025)

### Enum Implementation
- ✅ `ExperienceLevel` enum replaces integer experience field
- ✅ Predefined skills list (150+ skills) with multi-select UI
- ✅ Industry-standard data validation
- ✅ Consistent data models across frontend and backend

### Recommendations Engine
- ✅ Automatic generation with intelligent fallback
- ✅ 24-hour caching in PostgreSQL
- ✅ Priority-based skill recommendations
- ✅ Skill gap analysis and alignment scoring

### AI Integration
- ✅ FastAPI microservice for AI insights (Port 8001)
- ✅ OpenAI GPT-4 integration for career advice
- ✅ Fallback mode for development without API key
- ✅ Context-aware recommendations

### Data Sources
- ✅ Mock Data Service (active in dev mode)
- 🚧 Real API integrations planned (RapidAPI, LinkedIn, GitHub Jobs)

**Status**: Phase 2 Complete ✅
**Next**: Real API Integration & Production Deployment 🚧
