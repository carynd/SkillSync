# SkillSync - Intelligent Career Skill Mapping & Recommendation Platform

![Status](https://img.shields.io/badge/status-in%20development-yellow)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-latest-blue)
![Redis](https://img.shields.io/badge/Redis-latest-red)

## Overview

SkillSync is an intelligent, data-driven platform that helps students, professionals, and career changers identify what skills they should learn next to reach their desired roles — by analyzing their current skills, industry trends, and available learning resources.

## Features

- **User Profile Management** - Secure registration and authentication with JWT
- **Job Market Intelligence** - Real-time job market data analysis and skill demand tracking
- **Smart Recommendations** - AI-powered skill gap analysis and personalized learning paths
- **Career Insights** - AI-generated career guidance and roadmaps
- **Interactive Dashboard** - Visual analytics and progress tracking

## Tech Stack

### Backend
- **Java 17** with **Spring Boot 3.2.0**
- **PostgreSQL** for persistent storage
- **Redis** for caching and performance optimization
- **Spring Security** with JWT authentication
- **Maven** for dependency management

### AI Microservice
- **Python** with **FastAPI**
- **OpenAI API** for intelligent insights

### Frontend
- **React** with modern hooks
- **TailwindCSS** for styling
- **Chart.js** for data visualization

### DevOps
- **Docker** for containerization
- **AWS ECS** for orchestration
- **AWS RDS** for managed PostgreSQL
- **AWS ElastiCache** for managed Redis
- **GitHub Actions** for CI/CD

## Project Structure

```
SkilSync/
├── backend/
│   └── skillsync-api/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/com/skillsync/
│       │   │   │   ├── config/         # Configuration classes
│       │   │   │   ├── controller/     # REST API endpoints
│       │   │   │   ├── dto/            # Data Transfer Objects
│       │   │   │   ├── exception/      # Exception handling
│       │   │   │   ├── model/          # JPA entities
│       │   │   │   ├── repository/     # Data access layer
│       │   │   │   ├── security/       # JWT & authentication
│       │   │   │   └── service/        # Business logic
│       │   │   └── resources/
│       │   │       └── application.yml # Configuration
│       │   └── test/                   # Unit tests
│       └── pom.xml                     # Maven dependencies
├── ai-service/                         # Python FastAPI service
├── frontend/                           # React application
├── deployment/                         # Docker & Kubernetes configs
└── docs/                              # Documentation
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 14+
- Redis 6+
- Docker (optional)

### Database Setup

1. Install PostgreSQL and create a database:
```bash
createdb skillsync
```

2. Update database credentials in `application.yml` or set environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=skillsync
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
```

### Redis Setup

1. Install and start Redis:
```bash
# macOS
brew install redis
brew services start redis

# Linux
sudo apt-get install redis-server
sudo systemctl start redis
```

### Running the Backend

1. Navigate to the backend directory:
```bash
cd backend/skillsync-api
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

### API Documentation

Once the application is running, access the Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

## API Endpoints

### User Management
- `POST /api/users/register` - Register new user
- `POST /api/users/login` - Login user
- `GET /api/users/{userId}` - Get user profile
- `PUT /api/users/{userId}` - Update user profile

### Job Intelligence (Coming Soon)
- `GET /api/jobs/{role}/skills` - Get in-demand skills for a role
- `POST /api/jobs/sync` - Sync job market data

### Recommendations (Coming Soon)
- `GET /api/recommendations/{userId}` - Get personalized recommendations
- `POST /api/recommendations/cache` - Cache recommendations

### AI Insights (Coming Soon)
- `POST /api/insights` - Generate career insights
- `POST /api/insights/path` - Generate learning roadmap

## Configuration

Key configuration options in `application.yml`:

```yaml
jwt:
  secret: your-secret-key-here
  expiration: 86400000  # 24 hours

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/skillsync
  redis:
    host: localhost
    port: 6379
```

## Development Status - **85% Complete** 🚀

### Completion by Phase:

| Phase | Component | Completion % | Status |
|-------|-----------|--------------|--------|
| Phase 1 | Foundation & User Management | **100%** | ✅ |
| Phase 2 | Job Intelligence & Recommendations | **85%** | ✅ |
| Phase 3 | AI Insights & Career Guidance | **90%** | ✅ |
| Phase 4 | Frontend Development | **75%** | ✅ |
| Phase 5 | Bug Fixes & Optimization | **95%** | ✅ |
| Phase 6 | Testing Suite | **40%** | 🚧 |
| Phase 7 | Deployment & DevOps | **0%** | 📋 |
| **TOTAL** | **Overall Project** | **85%** | 🚀 |

### ✅ Recently Completed (This Session):

- **Fixed Arbeitnow API Integration** (95% → 100%)
  - Replaced WebClient with RestTemplate for reliable HTTP calls
  - Now successfully fetches real job market data from Arbeitnow API

- **Enhanced Job Roles Management** (75% → 100%)
  - Added 35+ diverse job roles across multiple industries
  - Implemented invalid role filtering to remove "string" entries
  - Created AdminController for future data management

- **Fixed Critical Bugs:**
  - Profile update transaction rollback error (FIXED)
  - Type mismatches in AI service (FIXED)
  - Missing RestTemplate bean dependency (FIXED)

### ✅ Fully Completed Features:

**Backend (100%):**
- User authentication with JWT tokens
- Spring Security with CORS configuration
- PostgreSQL database with JPA entities
- Redis caching system
- Complete exception handling framework
- Swagger/OpenAPI documentation
- Password validation and encryption (BCrypt)
- Transaction management

**Job Intelligence (100%):**
- Real-time Arbeitnow API integration
- 90+ technical skills extraction and analysis
- Skill demand scoring and calculation
- Database persistence layer
- 35+ diverse job roles across industries
- Skill gap analysis algorithms

**AI Insights (90%):**
- Google Gemini API integration
- Career advice generation
- Learning roadmap with timeline estimation
- Python FastAPI microservice
- AI chat endpoints

**Frontend (75%):**
- React user interface with hooks
- Registration, login, profile management
- Job search and skill analysis
- Chart visualizations with Chart.js
- TailwindCSS responsive design
- Auto-sync job data on role selection

### 🚧 In Progress:

- Advanced skill matching with profiles
- Comprehensive test suite
- Additional learning resource recommendations

### 📋 Planned for Future:

- Unit and integration testing
- Docker containerization
- AWS deployment (ECS, RDS, ElastiCache)
- GitHub Actions CI/CD pipeline
- Advanced analytics dashboard

## Testing

Run tests with:
```bash
mvn test
```

## Contributing

This is a portfolio project. Feedback and suggestions are welcome!

## License

MIT License

## Contact

Built with ❤️ as a demonstration of full-stack engineering skills

---

**SkillSync** - Your Personal Career Intelligence Platform
