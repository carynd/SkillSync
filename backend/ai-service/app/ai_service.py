import google.generativeai as genai
from typing import Dict, List
from app.config import settings
from app.models import CareerAdviceRequest, CareerAdviceResponse
from datetime import datetime
import logging

logger = logging.getLogger(__name__)


class AIInsightsService:
    """Service for generating AI-powered career insights using Google Gemini"""

    def __init__(self):
        if settings.gemini_api_key:
            genai.configure(api_key=settings.gemini_api_key)
            self.model = genai.GenerativeModel('gemini-2.5-flash')
            self.configured = True
            logger.info("Google Gemini API configured successfully")
        else:
            self.model = None
            self.configured = False
            logger.warning("Gemini API key not configured - using mock responses")

    def generate_career_advice(self, request: CareerAdviceRequest) -> CareerAdviceResponse:
        """Generate personalized career advice using Google Gemini"""

        if not self.configured:
            return self._generate_mock_advice(request)

        try:
            # Build context from user profile and skill gap
            context = self._build_context(request)

            # Create simpler prompt that doesn't trigger safety filters
            full_prompt = f"""You are a professional career development advisor. Help this person.

Current Role: {context['current_role']} ({context['experience']} years)
Target Role: {context['target_role']}
Current Skills: {', '.join(context['current_skills'][:5])}
Missing Skills: {', '.join(context['missing_skills'][:3])}

Question: {request.question}

Provide practical career advice with:
1. Main recommendation
2. Why it matters
3. 3-5 action steps
4. Timeline"""

            # Call Gemini API without forcing JSON (simpler, less likely to be blocked)
            response = self.model.generate_content(
                full_prompt,
                generation_config=genai.types.GenerationConfig(
                    temperature=0.7,
                    max_output_tokens=1000,
                )
            )

            # Extract text from response
            if response.parts and response.text:
                advice_text = response.text
                logger.info("Successfully generated personalized AI advice from Gemini")
            else:
                finish_reason = response.candidates[0].finish_reason if response.candidates else 'unknown'
                logger.warning(f"Gemini response blocked. Finish reason: {finish_reason}. Using personalized fallback.")
                return self._generate_personalized_fallback(request)

            # Parse the text response
            parsed = self._parse_ai_response(advice_text)

            return CareerAdviceResponse(
                advice=parsed.get("advice", ""),
                reasoning=parsed.get("reasoning", ""),
                action_items=parsed.get("action_items", []),
                estimated_timeline=parsed.get("timeline"),
                confidence_score=0.90,
                generated_at=datetime.now()
            )

        except Exception as e:
            logger.error(f"Error generating AI advice: {str(e)}")
            return self._generate_personalized_fallback(request)

    def _build_context(self, request: CareerAdviceRequest) -> Dict:
        """Build context from user data"""
        return {
            "name": request.user_profile.name,
            "current_role": request.user_profile.current_role,
            "target_role": request.user_profile.target_role,
            "experience": request.user_profile.experience_years,
            "current_skills": request.user_profile.current_skills,
            "skill_gap": request.skill_gap.skill_gap_percentage,
            "alignment": request.skill_gap.alignment_score,
            "missing_skills": request.skill_gap.missing_skills,
            "matching_count": request.skill_gap.matching_skills_count
        }

    def _get_system_prompt(self) -> str:
        """System prompt for the AI"""
        return """You are an expert career advisor specializing in technology careers.
Your role is to provide practical, actionable career guidance based on:
- User's current role and experience
- Their target career goal
- Their skill gaps and strengths
- Current job market trends

Provide advice that is:
1. Specific and actionable
2. Based on realistic timelines
3. Prioritized by importance
4. Encouraging but honest
5. Backed by reasoning

Format your response with:
- Main advice/recommendation
- Clear reasoning
- 3-5 specific action items
- Estimated timeline if applicable"""

    def _create_prompt(self, context: Dict, question: str) -> str:
        """Create the user prompt"""
        return f"""
Career Profile:
- Current Position: {context['current_role']} with {context['experience']} years experience
- Career Goal: {context['target_role']}
- Existing Skills: {', '.join(context['current_skills'][:10])}
- Skills to Learn: {', '.join(context['missing_skills'][:5])}
- Progress: {context['alignment']:.0f}% aligned with goal

Question: {question}

Please provide practical career development advice with specific action steps and estimated timeline.
"""

    def _parse_ai_response(self, response_text: str) -> Dict:
        """Parse AI response into structured format"""
        # Simple parsing - in production, could use more sophisticated parsing
        lines = response_text.split('\n')

        advice = ""
        reasoning = ""
        action_items = []
        timeline = None

        current_section = "advice"

        for line in lines:
            line = line.strip()
            if not line:
                continue

            if "reasoning" in line.lower() or "why" in line.lower():
                current_section = "reasoning"
                continue
            elif "action" in line.lower() or "steps" in line.lower():
                current_section = "actions"
                continue
            elif "timeline" in line.lower():
                current_section = "timeline"
                continue

            if current_section == "advice":
                advice += line + " "
            elif current_section == "reasoning":
                reasoning += line + " "
            elif current_section == "actions":
                if line.startswith(('-', '•', '*', '1', '2', '3', '4', '5')):
                    action_items.append(line.lstrip('-•* 123456789.'))
            elif current_section == "timeline" and not timeline:
                timeline = line

        return {
            "advice": advice.strip() or response_text[:200],
            "reasoning": reasoning.strip() or "Based on your profile and market trends",
            "action_items": action_items if action_items else ["Review the advice above", "Create a learning plan", "Start with highest priority skill"],
            "timeline": timeline
        }

    def _generate_mock_advice(self, request: CareerAdviceRequest) -> CareerAdviceResponse:
        """Generate mock advice when OpenAI is not configured"""
        logger.info("Generating mock career advice (OpenAI not configured)")

        missing = request.skill_gap.missing_skills[:3]

        advice = f"""Based on your goal to become a {request.user_profile.target_role},
I recommend focusing on {', '.join(missing)} first. These skills have high market demand
and will significantly improve your alignment score from {request.skill_gap.alignment_score:.1f}%."""

        reasoning = f"""With {request.user_profile.experience_years} years of experience in
{request.user_profile.current_role}, you have a solid foundation. Your current skill gap
of {request.skill_gap.skill_gap_percentage:.1f}% is manageable with focused learning."""

        action_items = [
            f"Start with {missing[0]} - highest priority skill",
            "Dedicate 10-15 hours per week to learning",
            "Build a portfolio project demonstrating the new skill",
            "Join relevant communities and forums",
            "Apply for junior positions once you've covered 50% of the gap"
        ]

        return CareerAdviceResponse(
            advice=advice,
            reasoning=reasoning,
            action_items=action_items,
            estimated_timeline="3-6 months with consistent effort",
            confidence_score=0.75,
            generated_at=datetime.now()
        )

    def _generate_fallback_advice(self, request: CareerAdviceRequest) -> CareerAdviceResponse:
        """Fallback advice if API call fails"""
        return CareerAdviceResponse(
            advice="Focus on bridging your skill gap systematically, starting with the highest-demand skills.",
            reasoning="General career progression advice based on typical patterns.",
            action_items=[
                "Review your missing skills list",
                "Prioritize by market demand",
                "Start learning the top 2-3 skills",
                "Build practical projects",
                "Network with professionals in your target role"
            ],
            estimated_timeline="3-6 months",
            confidence_score=0.5,
            generated_at=datetime.now()
        )

    def _generate_personalized_fallback(self, request: CareerAdviceRequest) -> CareerAdviceResponse:
        """Generate personalized fallback advice when Gemini API is blocked or fails"""
        logger.info("Generating personalized fallback advice")

        missing = request.skill_gap.missing_skills[:3]

        advice = f"""To transition from {request.user_profile.current_role} to {request.user_profile.target_role},
focus on learning {', '.join(missing)} first. These skills are in high demand and directly needed for your target role.
With your {request.user_profile.experience_years} years of experience, you have a strong foundation to build upon."""

        reasoning = f"""Your current skill alignment is {request.skill_gap.alignment_score:.0f}%, meaning you already have
some relevant skills. Your skill gap of {request.skill_gap.skill_gap_percentage:.0f}% is very achievable with focused
effort. Prioritizing high-demand skills will maximize your career growth."""

        action_items = [
            f"Master {missing[0]} - the most critical skill for your target role",
            f"Learn {missing[1]} to complement your new expertise",
            "Build 2-3 portfolio projects showcasing these new skills",
            f"Join communities focused on {request.user_profile.target_role}",
            "Apply to roles that value your current expertise + new skills"
        ]

        return CareerAdviceResponse(
            advice=advice,
            reasoning=reasoning,
            action_items=action_items,
            estimated_timeline="4-6 months with 10-15 hours per week",
            confidence_score=0.85,
            generated_at=datetime.now()
        )
