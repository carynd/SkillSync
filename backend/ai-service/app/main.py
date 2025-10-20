from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.models import (
    CareerAdviceRequest,
    CareerAdviceResponse,
    HealthCheck
)
from app.ai_service import AIInsightsService
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Create FastAPI app
app = FastAPI(
    title="SkillSync AI Insights Service",
    description="AI-powered career guidance and skill recommendations",
    version="1.0.0"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize AI service
ai_service = AIInsightsService()


@app.get("/", response_model=HealthCheck)
async def root():
    """Health check endpoint"""
    return HealthCheck(
        status="healthy",
        service="SkillSync AI Insights",
        version="1.0.0",
        openai_configured=ai_service.configured
    )


@app.get("/health", response_model=HealthCheck)
async def health_check():
    """Detailed health check"""
    return HealthCheck(
        status="healthy",
        service="SkillSync AI Insights",
        version="1.0.0",
        openai_configured=ai_service.configured
    )


@app.post("/api/ai/advice", response_model=CareerAdviceResponse)
async def get_career_advice(request: CareerAdviceRequest):
    """
    Generate AI-powered career advice based on user profile and skill gap.

    This endpoint uses Google Gemini to provide personalized career guidance.
    If Gemini is not configured, it returns smart mock advice.
    """
    try:
        logger.info(f"Generating career advice for user: {request.user_profile.name}")
        logger.info(f"Question: {request.question}")

        advice = ai_service.generate_career_advice(request)

        logger.info("Successfully generated career advice")
        return advice

    except Exception as e:
        logger.error(f"Error generating career advice: {str(e)}")
        raise HTTPException(
            status_code=500,
            detail=f"Failed to generate career advice: {str(e)}"
        )


@app.get("/api/ai/status")
async def get_service_status():
    """Get detailed service status"""
    return {
        "service": "AI Insights Service",
        "status": "running",
        "openai_configured": ai_service.configured,
        "backend_url": settings.backend_url,
        "features": {
            "career_advice": True,
            "skill_recommendations": ai_service.configured,
            "mock_mode": not ai_service.configured
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host=settings.service_host,
        port=settings.service_port,
        reload=True
    )
