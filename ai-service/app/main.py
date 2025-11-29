"""
SkillSync AI Service
FastAPI application for AI-powered career advice
"""

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import logging
import os
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize FastAPI app
app = FastAPI(title="SkillSync AI Service", version="1.0.0")

# Configure CORS to allow requests from frontend and backend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://localhost:5173", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Initialize Gemini client
gemini_model = None

def init_gemini():
    """Initialize Gemini API client"""
    global gemini_model
    try:
        import google.generativeai as genai
        api_key = os.getenv("GEMINI_API_KEY")
        if not api_key:
            logger.warning("GEMINI_API_KEY not set in environment - AI responses will be limited")
            return False

        genai.configure(api_key=api_key)
        gemini_model = genai.GenerativeModel("gemini-pro")
        logger.info("Gemini API initialized successfully")
        return True
    except ImportError:
        logger.warning("google-generativeai not installed - AI responses will be limited")
        return False
    except Exception as e:
        logger.error(f"Failed to initialize Gemini: {e}")
        return False

@app.on_event("startup")
async def startup_event():
    """Initialize services on startup"""
    logger.info("SkillSync AI Service starting up...")
    init_gemini()

@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "SkillSync AI Service"}

@app.post("/api/career-advice")
async def get_career_advice(request: dict):
    """
    Get career advice from Gemini AI

    Receives:
    - message: User's question/message
    - context: Rich context about user and conversation
    - preferences: User's AI preferences (context level, response style, tone)

    Returns:
    - response: AI-generated career advice
    - token_count: Number of tokens used
    - finish_reason: Why the model stopped generating (STOP, LENGTH, etc)
    """
    try:
        message = request.get("message", "")
        context = request.get("context", "")
        preferences = request.get("preferences", {})

        if not message:
            raise HTTPException(status_code=400, detail="Message is required")

        logger.info(f"Received career advice request")

        # Build prompt
        prompt = build_prompt(message, context, preferences)

        # Try to use Gemini if available
        if gemini_model:
            try:
                response = gemini_model.generate_content(
                    prompt,
                    generation_config={
                        "temperature": 0.7,
                        "top_p": 0.9,
                        "max_output_tokens": 1024,
                    }
                )

                ai_response = response.text
                token_count = estimate_token_count(ai_response)
                finish_reason = "STOP"

                logger.info(f"Received response from Gemini (tokens: {token_count})")

                return {
                    "response": ai_response,
                    "token_count": token_count,
                    "finish_reason": finish_reason
                }
            except Exception as e:
                logger.error(f"Gemini API error: {e}, falling back to mock response")
                return get_mock_response(message, preferences)
        else:
            # Fallback to mock response
            return get_mock_response(message, preferences)

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"Error getting career advice: {e}")
        raise HTTPException(status_code=500, detail="Failed to get career advice")

def get_mock_response(message: str, preferences: dict) -> dict:
    """
    Provide a mock response when Gemini is unavailable
    This is for testing and demo purposes
    """
    logger.info("Using mock response (Gemini unavailable)")

    tone = preferences.get("tone", "MENTORING").lower()
    response_style = preferences.get("response_style", "BALANCED").lower()

    mock_advice = f"""Based on your question: "{message[:50]}{'...' if len(message) > 50 else ''}"

I appreciate this question! Here's some career guidance:

1. **Assess Your Current Position**: Take time to evaluate where you are in your career journey. Consider your strengths, skills, and the value you bring to the table.

2. **Define Clear Goals**: What are your career objectives? Having clear, measurable goals helps you stay focused and motivated. Whether it's a promotion, skill development, or a career change, clarity is key.

3. **Continuous Learning**: The job market is always evolving. Invest in learning new skills, especially in areas that interest you. Online courses, certifications, and hands-on projects are great ways to grow.

4. **Build Your Network**: Connect with professionals in your field. Attend industry events, join online communities, and maintain relationships. Your network can open doors and provide valuable insights.

5. **Seek Feedback**: Regularly ask for feedback from mentors, managers, and colleagues. This helps you understand how others perceive your work and where you can improve.

Remember, career growth is a marathon, not a sprint. Stay patient, keep learning, and don't hesitate to seek guidance from mentors or career coaches. You've got this! 🚀

---

*Note: This is a demo response. For full AI-powered advice, please set up your Gemini API key.*
"""

    return {
        "response": mock_advice,
        "token_count": estimate_token_count(mock_advice),
        "finish_reason": "MOCK"
    }

def build_prompt(message: str, context: str, preferences: dict) -> str:
    """
    Build the prompt for Gemini API with context and preferences
    """
    prompt = ""

    # Add system instructions based on preferences
    if preferences:
        context_level = preferences.get("context_level", "BALANCED")
        response_style = preferences.get("response_style", "BALANCED")
        tone = preferences.get("tone", "MENTORING")

        prompt += f"Context Level: {context_level}\n"
        prompt += f"Response Style: {response_style}\n"
        prompt += f"Tone: {tone}\n\n"

    # Add context if provided
    if context:
        prompt += "Context:\n"
        prompt += context
        prompt += "\n\n"

    # Add user message
    prompt += f"User Question:\n{message}"

    return prompt

def estimate_token_count(text: str) -> int:
    """
    Estimate token count using rough approximation
    Real Gemini uses more sophisticated tokenization
    """
    # Rough estimate: ~4 characters per token
    return max(1, len(text) // 4)

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": "SkillSync AI Service",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "career_advice": "/api/career-advice"
        }
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
