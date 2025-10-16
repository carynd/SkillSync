# SkillSync AI Insights Service

AI-powered career guidance microservice using FastAPI and OpenAI GPT-4.

## Features

- 🤖 **AI Career Advice**: Get personalized career guidance based on your profile
- 📊 **Skill Gap Analysis**: Understand what skills to focus on
- 🎯 **Action Plans**: Receive specific, actionable recommendations
- ⚡ **Fast**: Built with FastAPI for high performance
- 🔄 **Mock Mode**: Works without OpenAI API key for development

## Setup

### 1. Install Python Dependencies

```bash
cd backend/ai-service
pip install -r requirements.txt
```

### 2. Configure Environment Variables

```bash
cp .env.example .env
# Edit .env and add your OpenAI API key
```

### 3. Run the Service

```bash
python run.py
```

The service will start on `http://localhost:8001`

## API Endpoints

### Health Check
```
GET /health
```

### Get Career Advice
```
POST /api/ai/advice
```

**Request Body:**
```json
{
  "user_profile": {
    "user_id": "123",
    "name": "John Doe",
    "current_role": "Junior Developer",
    "target_role": "Backend Engineer",
    "current_skills": ["Java", "Spring Boot"],
    "experience_years": 1
  },
  "skill_gap": {
    "skill_gap_percentage": 80.0,
    "alignment_score": 20.0,
    "missing_skills": ["Kubernetes", "AWS", "Docker"],
    "matching_skills_count": 2,
    "missing_skills_count": 8
  },
  "question": "Should I learn Kubernetes or AWS first?"
}
```

**Response:**
```json
{
  "advice": "I recommend starting with AWS first...",
  "reasoning": "AWS is more foundational...",
  "action_items": [
    "Start with AWS EC2 basics",
    "Learn S3 for storage",
    "Then move to Kubernetes"
  ],
  "estimated_timeline": "6-8 weeks",
  "confidence_score": 0.85,
  "generated_at": "2025-10-16T10:00:00"
}
```

## Development

### With OpenAI API Key
Set `OPENAI_API_KEY` in `.env` file to use real GPT-4 responses.

### Without OpenAI (Mock Mode)
Leave `OPENAI_API_KEY` empty - the service will return smart mock responses based on skill gap data.

## API Documentation

Interactive API docs available at:
- Swagger UI: `http://localhost:8001/docs`
- ReDoc: `http://localhost:8001/redoc`

## Architecture

```
ai-service/
├── app/
│   ├── __init__.py
│   ├── main.py          # FastAPI app & routes
│   ├── models.py        # Pydantic models
│   ├── config.py        # Configuration
│   └── ai_service.py    # OpenAI integration
├── requirements.txt
├── run.py
└── .env
```

## Integration with Spring Boot

The AI service runs independently on port 8001 and can be called from the Spring Boot backend (port 8080) via HTTP requests.

Example from Spring Boot:
```java
WebClient client = WebClient.create("http://localhost:8001");
CareerAdviceResponse response = client.post()
    .uri("/api/ai/advice")
    .bodyValue(request)
    .retrieve()
    .bodyToMono(CareerAdviceResponse.class)
    .block();
```
