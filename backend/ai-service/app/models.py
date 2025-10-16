from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime


class UserProfile(BaseModel):
    """User profile information"""
    user_id: str
    name: str
    current_role: str
    target_role: str
    current_skills: List[str]
    experience_years: int


class SkillGapInfo(BaseModel):
    """Skill gap analysis data"""
    skill_gap_percentage: float
    alignment_score: float
    missing_skills: List[str]
    matching_skills_count: int
    missing_skills_count: int


class CareerAdviceRequest(BaseModel):
    """Request for AI career advice"""
    user_profile: UserProfile
    skill_gap: SkillGapInfo
    question: str = Field(..., description="User's specific question")


class CareerAdviceResponse(BaseModel):
    """AI-generated career advice"""
    advice: str
    reasoning: str
    action_items: List[str]
    estimated_timeline: Optional[str] = None
    confidence_score: float = Field(..., ge=0.0, le=1.0)
    generated_at: datetime


class HealthCheck(BaseModel):
    """Health check response"""
    status: str
    service: str
    version: str
    openai_configured: bool
