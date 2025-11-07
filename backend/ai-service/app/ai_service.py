import google.generativeai as genai
from typing import Dict, List, Optional
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
        """Generate personalized career advice using Google Gemini with user context"""

        if not self.configured:
            return self._generate_mock_advice(request)

        try:
            # Build rich context from user profile and skill gap
            context = self._build_context(request)

            # Log the actual data being used
            logger.info(f"=== AI ADVICE REQUEST ===")
            logger.info(f"User: {request.user_profile.name} ({request.user_profile.user_id})")
            logger.info(f"Current Role: {request.user_profile.current_role}")
            logger.info(f"Target Role: {request.user_profile.target_role}")
            logger.info(f"Current Skills: {request.user_profile.current_skills}")
            logger.info(f"Missing Skills: {request.skill_gap.missing_skills}")
            logger.info(f"Question: {request.question}")
            logger.info(f"Skill Alignment: {request.skill_gap.alignment_score}%")
            logger.info(f"Skill Gap: {request.skill_gap.skill_gap_percentage}%")

            # Build a rich, contextual prompt that lets Gemini understand the user's situation
            # Include all user context so Gemini can answer any question with awareness
            system_context = f"""You are an expert career advisor and technical mentor for skill development.
You are helping {request.user_profile.name} with their career transition and learning journey.

USER PROFILE:
- Current Role: {request.user_profile.current_role} ({request.user_profile.experience_years} years experience)
- Target Role: {request.user_profile.target_role}
- Current Skills: {', '.join(request.user_profile.current_skills)}
- Experience Level: {request.user_profile.experience_years} years in tech
- Skills Goal Alignment: {request.skill_gap.alignment_score:.0f}% (how well current skills match target role)
- Skill Gap: {request.skill_gap.skill_gap_percentage:.0f}% (skills still needed)
- Missing Key Skills: {', '.join(request.skill_gap.missing_skills)}

Your role is to provide personalized, contextual advice that:
1. Acknowledges their current position and experience
2. Understands their specific goal (transition to {request.user_profile.target_role})
3. References their specific skill gaps and missing skills
4. Provides actionable, practical guidance
5. Is encouraging but realistic about the learning journey

Answer their question directly and naturally, incorporating their personal context whenever relevant.
Provide specific resources, project ideas, learning strategies, and timelines based on their situation.
Be conversational and helpful, not formulaic."""

            user_question = f"Question: {request.question}"

            logger.info(f"Attempting Gemini API call with rich context prompt...")

            try:
                response = self.model.generate_content(
                    f"{system_context}\n\n{user_question}",
                    generation_config=genai.types.GenerationConfig(
                        temperature=0.7,
                        max_output_tokens=1000,
                    )
                )

                # Extract text from response
                if response.parts and response.text:
                    advice_text = response.text
                    logger.info("✅ Successfully generated advice from Gemini API")
                else:
                    finish_reason = response.candidates[0].finish_reason if response.candidates else 'unknown'
                    logger.warning(f"⚠️  Gemini blocked. Reason: {finish_reason}")
                    return self._generate_personalized_fallback(request)

            except Exception as gemini_error:
                logger.error(f"⚠️  Gemini API error: {str(gemini_error)}")
                return self._generate_personalized_fallback(request)

            # For Gemini-generated responses, return only the formatted advice
            # Clean markdown rendering and exclude metadata fields
            return CareerAdviceResponse(
                advice=self._format_advice_markdown(advice_text),
                generated_at=datetime.now()
            )

        except Exception as e:
            logger.error(f"Error generating AI advice: {str(e)}")
            return self._generate_personalized_fallback(request)

    def _format_advice_markdown(self, advice: str) -> str:
        """Format advice text to ensure markdown rendering properly"""
        # Clean up the advice text to ensure proper markdown formatting
        # Replace ** with proper formatting that will render in markdown
        lines = []
        for line in advice.split('\n'):
            # Keep the line as is - markdown will handle **bold** rendering
            lines.append(line)
        return '\n'.join(lines)

    def _extract_action_items(self, text: str) -> List[str]:
        """Extract action items from Gemini response text"""
        action_items = []
        lines = text.split('\n')
        in_action_section = False

        for line in lines:
            line_lower = line.lower()
            # Detect action section headers
            if any(keyword in line_lower for keyword in ['action', 'steps', 'next', 'do:', 'try:']):
                in_action_section = True
                continue

            # Extract bullet points and numbered items
            if in_action_section and line.strip():
                if line.strip()[0] in '-•*123456789':
                    action_items.append(line.strip().lstrip('-•* 0123456789.))'))
                elif line.strip()[0] not in '-•*0123456789' and action_items:
                    # Stop collecting if we hit non-action content
                    break

        # If no actions found, generate some from the main text
        if not action_items:
            action_items = [
                "Review the guidance above",
                "Create a learning plan based on your situation",
                "Start with the highest priority skill",
                "Build projects to reinforce learning",
                "Network with professionals in your target role"
            ]

        return action_items[:5]  # Return top 5

    def _extract_timeline(self, text: str) -> Optional[str]:
        """Extract timeline information from Gemini response text"""
        import re

        # Look for common timeline patterns
        patterns = [
            r'(\d+)\s*-\s*(\d+)\s*(?:weeks?|months?|years?)',
            r'(?:about|approximately|around)\s*(\d+)\s*(?:weeks?|months?|years?)',
            r'(\d+)\s*(?:weeks?|months?|years?)\s*(?:to|of)',
        ]

        for pattern in patterns:
            match = re.search(pattern, text, re.IGNORECASE)
            if match:
                return match.group(0)

        return None

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

    def _answer_resource_question(self, request: CareerAdviceRequest, missing_skills: List[str]) -> CareerAdviceResponse:
        """Provide curated learning resources and GitHub project references for requested skills"""
        logger.info("📚 Answering resource/learning links question")

        # Curated resource database for common technical skills
        resources_db = {
            "sql": {
                "tutorials": [
                    "[SQL Tutorial - W3Schools](https://www.w3schools.com/sql/)",
                    "[SQLZoo - Interactive SQL Learning](https://sqlzoo.net/)",
                    "[Mode SQL Tutorial](https://mode.com/sql-tutorial/)",
                    "[Khan Academy - SQL](https://www.khanacademy.org/computing/computer-programming/sql)"
                ],
                "github_projects": [
                    "[sql-tutorial - SQL Examples & Exercises](https://github.com/sql-tutorial/sql-tutorial)",
                    "[awesome-sql - Curated list of SQL resources](https://github.com/danhuss/awesome-sql)",
                    "[SQLAlchemy ORM Examples](https://github.com/sqlalchemy/sqlalchemy)",
                    "[Real World SQL Projects](https://github.com/topics/sql-tutorial)"
                ]
            },
            "python": {
                "tutorials": [
                    "[Python Official Docs](https://docs.python.org/3/)",
                    "[Real Python Tutorials](https://realpython.com/)",
                    "[Codecademy - Learn Python](https://www.codecademy.com/learn/learn-python-3)",
                    "[DataCamp - Python Courses](https://www.datacamp.com/courses/intro-to-python-for-data-science)"
                ],
                "github_projects": [
                    "[awesome-python - Curated Python resources](https://github.com/vinta/awesome-python)",
                    "[TheAlgorithms/Python - Learn algorithms](https://github.com/TheAlgorithms/Python)",
                    "[real-python - Real Python project examples](https://github.com/realpython/)"
                ]
            },
            "javascript": {
                "tutorials": [
                    "[MDN JavaScript Guide](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide)",
                    "[JavaScript.info - JS fundamentals](https://javascript.info/)",
                    "[Eloquent JavaScript - Free Book](https://eloquentjavascript.net/)",
                    "[FreeCodeCamp - JavaScript](https://www.freecodecamp.org/learn/javascript/)"
                ],
                "github_projects": [
                    "[awesome-javascript - Curated JS resources](https://github.com/sorrycc/awesome-javascript)",
                    "[30-seconds-of-code - Code snippets](https://github.com/30-seconds/30-seconds-of-code)",
                    "[javascript-algorithms - Algorithms in JS](https://github.com/trekhleb/javascript-algorithms)"
                ]
            },
            "node.js": {
                "tutorials": [
                    "[Node.js Official Documentation](https://nodejs.org/en/docs/)",
                    "[Node.js Best Practices](https://github.com/goldbergyoni/nodebestpractices)",
                    "[Express.js Guide](https://expressjs.com/)",
                    "[Node.js Tutorial Point](https://www.tutorialspoint.com/nodejs/)"
                ],
                "github_projects": [
                    "[awesome-nodejs - Node resources](https://github.com/sindresorhus/awesome-nodejs)",
                    "[node-express-gen - Express starter](https://github.com/expressjs/generator)",
                    "[Real World Example Apps](https://github.com/gothinkster/realworld)"
                ]
            },
            "react": {
                "tutorials": [
                    "[React Official Documentation](https://react.dev/)",
                    "[React Tutorial - Interactive](https://react.dev/learn)",
                    "[Scrimba - Learn React Free](https://scrimba.com/learn/learnreact/)",
                    "[FreeCodeCamp React Course](https://www.freecodecamp.org/learn/front-end-development-libraries/react/)"
                ],
                "github_projects": [
                    "[awesome-react - React resources](https://github.com/enaqx/awesome-react)",
                    "[awesome-react-components - React components](https://github.com/brillout/awesome-react-components)",
                    "[React patterns examples](https://github.com/krasimir/react-patterns)"
                ]
            },
            "git": {
                "tutorials": [
                    "[Git Official Documentation](https://git-scm.com/doc)",
                    "[Atlassian Git Tutorials](https://www.atlassian.com/git/tutorials)",
                    "[GitHub Skills - Git Training](https://skills.github.com/)",
                    "[Pro Git Book - Free](https://git-scm.com/book/en/v2)"
                ],
                "github_projects": [
                    "[github/gitignore - .gitignore templates](https://github.com/github/gitignore)",
                    "[Oh Shit, Git!? - Git tips & tricks](https://ohshitgit.com/)",
                    "[Git Tips - Git hacks](https://github.com/git-tips/tips)"
                ]
            },
            "linux": {
                "tutorials": [
                    "[Linux Academy Free Courses](https://linuxacademy.org/)",
                    "[Linux Command Line Tutorial](https://ubuntu.com/tutorials)",
                    "[GNU/Linux Command-Line Tools](https://tldp.org/LDP/intro-linux/html/)",
                    "[Codeacademy - Command Line](https://www.codecademy.com/learn/learn-the-command-line)"
                ],
                "github_projects": [
                    "[awesome-linux - Linux resources](https://github.com/aleksandar-todorovic/awesome-linux)",
                    "[the-art-of-command-line](https://github.com/jlevy/the-art-of-command-line)",
                    "[Linux Bash Guide](https://github.com/Idnan/bash-guide)"
                ]
            },
            "docker": {
                "tutorials": [
                    "[Docker Official Documentation](https://docs.docker.com/)",
                    "[Docker for Beginners](https://docker-curriculum.com/)",
                    "[Play with Docker Playground](https://www.docker.com/play-with-docker/)",
                    "[Udemy - Docker Mastery](https://www.udemy.com/course/docker-mastery/)"
                ],
                "github_projects": [
                    "[awesome-docker - Docker resources](https://github.com/veggiemonk/awesome-docker)",
                    "[docker/getting-started](https://github.com/docker/getting-started)",
                    "[Docker best practices](https://github.com/FuriKuri/docker-best-practices)"
                ]
            },
            "kubernetes": {
                "tutorials": [
                    "[Kubernetes Official Docs](https://kubernetes.io/docs/)",
                    "[Kubernetes for Beginners](https://www.freecodecamp.org/news/learn-kubernetes-in-100-steps/)",
                    "[Katacoda K8s Scenarios](https://www.katacoda.com/courses/kubernetes/)",
                    "[Kubernetes by Example](https://kubernetesbyexample.com/)"
                ],
                "github_projects": [
                    "[awesome-kubernetes - K8s resources](https://github.com/ramitsurana/awesome-kubernetes)",
                    "[kubernetes/examples](https://github.com/kubernetes/examples)",
                    "[Kubernetes patterns](https://github.com/gravitational/k8spatterns)"
                ]
            }
        }

        # Build response with resources for the missing skills
        response_parts = []

        for skill in missing_skills[:3]:  # Focus on top 3 missing skills
            skill_lower = skill.lower()

            if skill_lower in resources_db:
                resource_info = resources_db[skill_lower]
                response_parts.append(f"## Free Resources to Learn {skill}\n")

                response_parts.append("### Official Documentation & Tutorials")
                for tutorial in resource_info["tutorials"]:
                    response_parts.append(f"- {tutorial}")

                response_parts.append("\n### GitHub Projects for Code Reference\n")
                for project in resource_info["github_projects"]:
                    response_parts.append(f"- {project}")

                response_parts.append("")
            else:
                # Fallback for skills not in our database
                response_parts.append(f"## Resources for {skill}\n")
                response_parts.append(f"### Suggested Learning Paths:")
                response_parts.append(f"- Search '{skill} tutorial' on YouTube for video guides")
                response_parts.append(f"- Check 'awesome-{skill_lower}' on GitHub for curated resources")
                response_parts.append(f"- Visit the official {skill} documentation")
                response_parts.append(f"- Search GitHub for '{skill} example' or '{skill} learning'")
                response_parts.append("")

        # Add general learning advice
        response_parts.append("## How to Use These Resources\n")
        response_parts.append("1. **Start with Official Docs** - Free and always the most authoritative")
        response_parts.append("2. **YouTube Tutorials** - Visual learning, great for understanding concepts")
        response_parts.append("3. **GitHub Projects** - Real code examples, study how others solve problems")
        response_parts.append("4. **Build Projects** - Apply what you learn immediately")
        response_parts.append("5. **Online Communities** - Ask questions, learn from others (Stack Overflow, Reddit /r/learnprogramming)")

        advice = "\n".join(response_parts)

        logger.info("✅ Generated resource-focused response")
        return CareerAdviceResponse(
            advice=advice,
            generated_at=datetime.now()
        )

    def _generate_personalized_fallback(self, request: CareerAdviceRequest) -> CareerAdviceResponse:
        """Generate personalized fallback advice when Gemini API is blocked or fails"""
        logger.info("⚠️  Gemini API blocked. Generating intelligent context-aware fallback response...")

        missing = request.skill_gap.missing_skills[:3]
        current_role = request.user_profile.current_role
        target_role = request.user_profile.target_role
        experience = request.user_profile.experience_years
        gap = request.skill_gap.skill_gap_percentage
        alignment = request.skill_gap.alignment_score
        question = request.question.lower()

        # Detect what type of question they're asking to provide targeted fallback
        # Check for resource/GitHub questions FIRST (most specific)
        is_resource_question = any(word in question for word in ["resource", "link", "github", "reference", "free", "example project", "code example", "tutorial", "course"])
        # Check for timeline questions (more specific)
        is_timeline_question = any(word in question for word in ["how long", "timeline", "months", "how much time", "how many months", "how many weeks", "take me"])
        # Check for learning/methodology questions
        is_learning_question = any(word in question for word in ["learn", "effective", "best way", "how should", "should i take"]) and not is_resource_question
        # Check for project questions
        is_project_question = any(word in question for word in ["project", "build", "create", "develop", "suggest", "ideas"])
        # Check for real-world context
        is_real_world = "real world" in question or "problem" in question

        # Generate response tailored to their actual question
        # Check in priority order: resource > timeline > real_world_projects > projects > learning > default
        if is_resource_question:
            return self._answer_resource_question(request, missing)

        if is_timeline_question:
            # Timeline expectations - prioritize this check
            advice = f"""Here's a realistic timeline for your transition to {target_role}:

**Expected Timeline:**
- **Weeks 1-2:** Learn basics of {missing[0]} with small projects
- **Weeks 3-4:** Build your first significant project using {missing[0]}
- **Weeks 5-6:** Learn {missing[1] if len(missing) > 1 else 'databases'} and integrate with projects
- **Weeks 7-8:** Polish projects, build portfolio, start applying
- **Weeks 9-12:** Keep applying, keep learning, interview

**Total: 3-4 months with 10-15 hours/week**

**Factors that speed things up:**
- Your {experience} years experience (you know programming fundamentals)
- Learning through projects vs courses (3x faster)
- Your frontend background (you understand half the stack already)

**Factors that extend the timeline:**
- Inconsistent learning (must be 10+ hours/week minimum)
- Learning too many skills at once (focus on {missing[0]} and {missing[1] if len(missing) > 1 else 'databases'} first)
- Waiting to be "perfect" before applying (start at week 8)

My honest assessment: With your {experience} years background and focused effort, you could have a solid {target_role} portfolio and start getting interviews in 10-12 weeks."""

        elif is_project_question and is_real_world:
            # Project ideas for real-world problems
            advice = f"""Great question! Here are realistic project ideas that solve real problems while teaching you {missing[0]} and {missing[1] if len(missing) > 1 else 'databases'}:

**Best Project Ideas:**

1. **Personal Finance Tracker** (4-6 weeks)
   - Problem: People struggle to track spending and budgets
   - Stack: React frontend + {missing[0]} backend + {missing[1] if len(missing) > 1 else 'PostgreSQL'} database
   - Hiring Value: Very high - shows full-stack and real-world thinking
   - Features: Income/expense tracking, budget alerts, spending insights

2. **Freelancer/Service Marketplace** (6-8 weeks)
   - Problem: Freelancers need better platforms to find clients
   - Stack: Full-stack platform with user authentication, payments, ratings
   - Hiring Value: Excellent - complex backend logic, real business needs
   - Shows you understand: User management, data relationships, business logic

3. **Study Group Coordinator** (4-5 weeks)
   - Problem: Students can't easily find study partners
   - Stack: React + {missing[0]} + {missing[1] if len(missing) > 1 else 'databases'}
   - Hiring Value: Good - shows you solve real problems

**My Recommendation:**
Start with **Personal Finance Tracker**. Why?
- Solves a problem everyone has (money management)
- Covers all the skills you need to learn ({missing[0]}, {missing[1] if len(missing) > 1 else 'databases'})
- Completes in 4-6 weeks (not months)
- Employers immediately understand the value
- Easy to show in interviews and github

Your {experience} years as {current_role} means you can build this 2x faster than someone starting from scratch. You already know how to structure code, debug, and think about user experience."""

        elif is_learning_question:
            # How to learn
            advice = f"""Here's the most effective way to learn {missing[0]}, {missing[1] if len(missing) > 1 else 'databases'}, and other missing skills:

**The Most Effective Learning Strategy:**

1. **Learn Through Projects, Not Courses**
   - Don't take a 10-hour {missing[0]} course
   - Instead: Build a project and learn {missing[0]} as you need it
   - You'll learn 5x faster this way

2. **Start with Official Docs**
   - {missing[0]} Official Documentation (free, always best)
   - Practice small examples
   - Then apply to your project

3. **The Learning Ratio That Works:**
   - 70% building projects
   - 20% watching tutorials/reading
   - 10% formal courses
   - Most people get this backwards!

4. **Timeline Per Skill:**
   - {missing[0]}: 3-4 weeks to competency (can build things with it)
   - {missing[1] if len(missing) > 1 else 'SQL'}: 2-3 weeks
   - {missing[2] if len(missing) > 2 else 'DevOps'}: 2-3 weeks

5. **Your Advantage:**
   You have {experience} years as {current_role}, so you:
   - Know how to structure code
   - Can debug effectively
   - Understand development workflows
   - This accelerates learning significantly

You don't need to learn theory. You need to learn syntax and patterns specific to {missing[0]}. That's totally doable in weeks, not months."""

        elif is_project_question:
            # General project advice
            advice = f"""Excellent! Here are some strong project ideas for your portfolio:

**Top Project Ideas for {target_role}:**

1. **Full-Stack Todo/Task Manager** - The classic, but still highly effective
   - Frontend: React + your CSS skills
   - Backend: {missing[0]}
   - Database: {missing[1] if len(missing) > 1 else 'SQL'}
   - Why: Covers all core skills, easy to explain

2. **E-commerce Product Catalog**
   - Product listings, search, filtering, cart functionality
   - Shows you understand complex data relationships
   - Demonstrates {target_role} thinking

3. **Real-time Chat Application**
   - WebSockets, real-time updates
   - User authentication
   - Shows advanced backend skills

4. **Job/Project Board**
   - Users post opportunities, others apply
   - Demonstrates notifications, rankings, search
   - Relevant to tech industry

**How to Choose:**
Pick the one that excites you most. You'll work faster and with more passion. Each of these can be completed in 3-6 weeks, creating a impressive portfolio piece.

With your {experience} years experience, you have the foundation to build any of these. Focus on making one really well rather than rushing through multiple half-built projects."""

        else:
            # Default: comprehensive answer
            advice = f"""Based on your transition from {current_role} to {target_role}, here's your path:

**Your Situation:**
- {experience} years of {current_role} experience
- {alignment:.0f}% aligned with {target_role} requirements
- Need to learn: {', '.join(missing)}

**The Strategy:**
1. **Pick a Real-World Project** that uses {missing[0]} and {missing[1] if len(missing) > 1 else 'databases'}
2. **Learn by Building** - Don't take courses, learn as you build
3. **Go Deep, Not Wide** - Master 2-3 skills well, not 10 skills poorly
4. **Build Your Portfolio** - Create 2-3 strong projects
5. **Apply and Interview** - Start applying after week 6-8

**Timeline:** 3-6 months, 10-15 hours per week

**Your Advantage:**
Your {experience} years as {current_role} is a huge advantage. You already understand:
- How to structure code and projects
- How to debug problems
- How development workflows work
- How to learn technical skills

You're not learning "how to code" - you're learning specific tools ({missing[0]}, {missing[1] if len(missing) > 1 else 'databases'}, etc). Much faster."""

        # Return only the formatted advice - no extra metadata fields
        return CareerAdviceResponse(
            advice=self._format_advice_markdown(advice),
            generated_at=datetime.now()
        )
