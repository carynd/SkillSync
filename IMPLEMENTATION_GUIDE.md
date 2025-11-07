# SkillSync AI Chat System - Implementation Guide

## Overview

This guide provides a complete roadmap for implementing the SkillSync AI Chat System following enterprise-grade standards and design patterns.

---

## Phase 1: Foundation (Week 1) ✅ COMPLETE

### Day 1: Database Schema & Core Models ✅
**Status:** COMPLETE
**Files:** 15 files created
**Lines of Code:** 2,500+ production code + 1,300+ documentation

**Deliverables:**
- ✅ V4 Database migration with enum tables, soft deletes, indexes, triggers, views
- ✅ 6 Enterprise enums (ConversationStatus, MessageRole, ContextLevel, ResponseStyle, AITone, PlanType)
- ✅ 3 JPA entities (ChatConversation, ChatMessage, UserAIPreferences)
- ✅ 3 DTOs (ChatMessageDTO, ChatConversationDTO, UserAIPreferencesDTO)
- ✅ 3 Documentation files (ARCHITECTURE.md, DAY_1_SUMMARY.md, QUICK_START_DAY_1.md)

**Key Patterns:**
- Enum tables for type safety
- Soft deletes for compliance
- Denormalization for performance
- Bidirectional enum mapping
- DTO pattern for API isolation

**Read These Files:**
1. `ARCHITECTURE.md` - Comprehensive architecture guide
2. `DAY_1_SUMMARY.md` - What was built
3. `QUICK_START_DAY_1.md` - Setup instructions
4. `DAY_1_DELIVERABLES.md` - Detailed breakdown

---

## Phase 2: Backend Services (Week 1, Day 2-3)

### Day 2: Repository & Service Layer (In Progress)
**Goal:** Build data access and business logic layer

**Files to Create:**
```
backend/skillsync-api/src/main/java/com/skillsync/

repository/
├── ChatConversationRepository.java        (Spring Data JPA)
├── ChatMessageRepository.java             (Custom queries)
├── UserAIPreferencesRepository.java       (One-to-one)
└── ChatSessionRepository.java             (Plan management)

service/
├── ChatService.java                       (Main chat logic)
├── PreferencesService.java                (User preferences)
├── ContextBuilderService.java             (Gemini context)
└── EngagementService.java                 (Analytics/nudges)

controller/
├── ChatController.java                    (REST endpoints)
├── ConversationController.java            (Conversation CRUD)
├── PreferencesController.java             (Preference CRUD)
└── HealthController.java                  (Health checks)

exception/
├── SkillSyncException.java                (Base exception)
├── ChatNotFoundException.java
├── UnauthorizedException.java
├── QuotaExceededException.java
└── GlobalExceptionHandler.java            (Error handling)
```

**Day 2 Checklist:**
- [ ] Create Repository interfaces
- [ ] Implement custom JPA queries
- [ ] Create Service classes with business logic
- [ ] Add @Transactional annotations
- [ ] Implement caching strategy
- [ ] Create exception hierarchy
- [ ] Add global exception handler
- [ ] Test repositories with unit tests

**Day 3 Checklist:**
- [ ] Create REST Controller classes
- [ ] Add request/response models
- [ ] Implement input validation
- [ ] Add authentication/authorization
- [ ] Implement rate limiting
- [ ] Add logging strategy
- [ ] Test APIs with Postman
- [ ] Document API endpoints

---

## Phase 3: AI Service Integration (Week 1, Day 3-4)

### Day 3: AI Service Redesign

**Current Status:** The old question-classification system needs replacement

**What to Change:**
```
OLD (Question Classification):
User Question → Detect Type → Choose Template → Return Response ❌

NEW (Full Context):
User Question → Load History → Load Preferences → Load Profile
    → Build System Prompt → Call Gemini Directly → Return Response ✅
```

**Files to Create/Update:**
```
backend/ai-service/app/

ai_service_v2.py                  (NEW: Full rewrite)
├── generate_response()           (Main entry point)
├── _load_context()               (Fetch all context)
├── _build_messages()             (Format for Gemini)
├── _call_gemini()                (API integration)
└── _handle_gemini_blocked()      (Graceful fallback)

(Delete old _answer_resource_question, _answer_project_question, etc)
```

**Key Changes:**
1. Remove `_generate_personalized_fallback()` with all question-type detection
2. Build ONE powerful system prompt with all user context
3. Let Gemini handle any question type naturally
4. Implement Redis caching for responses
5. Better fallback when Gemini is blocked

**System Prompt Template:**
```
You are an expert career advisor for SkillSync.

USER CONTEXT (if context_level != MINIMAL):
- Current Role: {{ current_role }}
- Target Role: {{ target_role }}
- Experience: {{ experience_years }} years
- Skills: {{ current_skills }}
- Goal Alignment: {{ alignment_score }}%

CONVERSATION HISTORY (last 10 messages):
{{ conversation_history }}

USER PREFERENCES:
- Style: {{ response_style }} (concise/balanced/detailed)
- Tone: {{ tone }} (professional/casual/mentoring)
- Include Resources: {{ include_resources }}
- Include Examples: {{ include_examples }}
- Include Timeline: {{ include_timeline }}

Answer naturally. Be conversational. Respect user preferences.
```

---

## Phase 4: Frontend Components (Week 1, Day 4-5)

### Day 4: Chat UI & Preferences Panel

**Components to Build:**
```
frontend/src/components/

ChatWindow/
├── ChatWindow.jsx                 (Main container)
├── MessageList.jsx                (Display messages)
├── InputBox.jsx                   (User input)
├── CustomizePanel.jsx             (⚙️ floating button + panel)
├── ChatSidebar.jsx                (Conversation list)
└── MessageItem.jsx                (Individual message)

Engagement/
├── ProgressWidget.jsx             (Skill progress tracker)
├── AchievementBadge.jsx           (Achievements/milestones)
├── OfflineMessage.jsx             (Gemini down gracefully)
└── NudgeNotification.jsx          (Re-engagement nudges)

Settings/
├── PresetButtons.jsx              (Study Mode, Interview Prep)
├── StyleSelector.jsx              (Concise/Balanced/Detailed)
└── FeatureToggles.jsx             (Include resources, etc)
```

**Day 4 Implementation:**
- [ ] Build ChatWindow main container
- [ ] Implement message display with timestamps
- [ ] Create input box with send button
- [ ] Build message list with pagination
- [ ] Add loading states and animations
- [ ] Implement typing indicators
- [ ] Add message deletion (soft delete on backend)
- [ ] Test basic message sending

**Day 5 Implementation:**
- [ ] Build CustomizePanel component
- [ ] Add preference toggle switches
- [ ] Implement quick presets
- [ ] Build ProgressWidget
- [ ] Add AchievementBadge system
- [ ] Implement OfflineMessage gracefully
- [ ] Add re-engagement nudges
- [ ] Style everything beautifully

---

## Phase 5: Integration & Testing (Week 1, Day 5-6)

### Day 5: Integration Testing

**Testing Checklist:**
```
Backend:
  [ ] Unit tests for repositories
  [ ] Unit tests for services
  [ ] Integration tests for controllers
  [ ] Exception handling tests
  [ ] Validation tests
  [ ] Quota/limit tests

Frontend:
  [ ] Component rendering tests
  [ ] User interaction tests
  [ ] API integration tests
  [ ] Error handling tests
  [ ] Offline mode tests

End-to-End:
  [ ] User creates conversation
  [ ] User sends message
  [ ] AI responds
  [ ] Response uses user preferences
  [ ] Preferences are saved
  [ ] Free tier limits enforced
  [ ] Gemini down → fallback works
```

**Test Scenarios:**
1. First-time user creates chat
2. User sends message, AI responds
3. User updates preferences
4. Preferences applied to next response
5. User creates 3rd chat (quota exceeded)
6. Gemini API fails, fallback works
7. Message pagination works
8. Soft delete works properly

---

## Phase 6: Polish & Deployment (Week 1, Day 6-7)

### Day 6: Error Handling & Monitoring

**Error Handling:**
```
Every layer should handle errors:

Controller:
  @ExceptionHandler(ChatNotFoundException.class)
  Handle and return proper HTTP status

Service:
  Catch specific exceptions, log, decide to retry/fail

Repository:
  Handle SQL errors, return empty/null appropriately

AI Service:
  Try Gemini, fallback to cache, fallback to engagement message
```

**Monitoring Points:**
- Gemini API latency
- Cache hit rate
- Quota limit approaching
- Error rates by type
- User engagement metrics
- Message token counts
- API response times

### Day 7: Production Deployment

**Deployment Checklist:**
- [ ] Database migrations applied
- [ ] Flyway version control
- [ ] Environment variables configured
- [ ] Gemini API key secured
- [ ] Redis cache configured
- [ ] MongoDB archive setup (future)
- [ ] Logging configured (ELK stack or similar)
- [ ] Monitoring/alerting setup
- [ ] Health checks verified
- [ ] Load testing completed
- [ ] Security audit
- [ ] Backup strategy
- [ ] Rollback plan

---

## Architecture Summary

```
┌────────────────────────────────────────────────────────────────┐
│                       FRONTEND (React)                         │
│  Chat UI + Preferences Panel + Engagement Widgets             │
└────────────────────┬───────────────────────────────────────────┘
                     │ REST APIs
┌────────────────────▼───────────────────────────────────────────┐
│              BACKEND SERVICES (Spring Boot)                    │
│                                                                 │
│  Controllers (REST endpoints)                                  │
│       ↓                                                        │
│  Services (Business Logic)                                    │
│       ↓                                                        │
│  Repositories (Data Access)                                   │
│       ↓                                                        │
│  Entities + DTOs                                              │
└────────────────────┬───────────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼──┐  ┌──────▼────┐  ┌──▼────────┐
│PostgreSQL│  │   Redis   │  │  Gemini   │
│(Hot Data)│  │  (Cache)  │  │   API     │
└──────────┘  └───────────┘  └───────────┘

┌────────────────────────────────────────┐
│  MongoDB (Archive - Old Messages)      │
└────────────────────────────────────────┘
```

---

## Database Hierarchy

```
Enum Tables (Type Safety)
  ├── conversation_status
  ├── message_role
  ├── context_level
  ├── response_style
  ├── ai_tone
  └── plan_type

Core Tables (Chat System)
  ├── chat_conversations (user's chats)
  ├── chat_messages (recent messages)
  ├── user_ai_preferences (customization)
  ├── chat_sessions (quota tracking)
  ├── ai_response_cache (fallback)
  └── user_engagement (analytics)
```

---

## API Endpoints (Day 2-3)

### Conversations
```
POST   /api/v1/conversations              Create new chat
GET    /api/v1/conversations              List chats (paginated)
GET    /api/v1/conversations/{id}         Get one chat
PATCH  /api/v1/conversations/{id}         Update title
DELETE /api/v1/conversations/{id}         Soft delete
```

### Messages
```
POST   /api/v1/conversations/{id}/messages    Send message
GET    /api/v1/conversations/{id}/messages    List messages (paginated)
GET    /api/v1/conversations/{id}/messages?search=keyword    Search
```

### Preferences
```
GET    /api/v1/preferences                Get user preferences
PATCH  /api/v1/preferences                Update preferences
POST   /api/v1/preferences/reset          Reset to defaults
POST   /api/v1/preferences/apply-preset   Apply preset
```

---

## Key Success Criteria

### Code Quality
- [ ] 100% type-safe (enums, no strings)
- [ ] 100% documented (JavaDoc + comments)
- [ ] 100% tested (unit + integration)
- [ ] 100% validated (input validation)
- [ ] Zero code duplication
- [ ] Clear separation of concerns

### Performance
- [ ] Response time < 2s
- [ ] Cache hit rate > 80%
- [ ] Database queries < 100ms
- [ ] Proper indexing used
- [ ] No N+1 query issues
- [ ] Pagination implemented

### Scalability
- [ ] Designed for millions of users
- [ ] Read replicas supported
- [ ] Horizontal scaling ready
- [ ] Load balancer compatible
- [ ] Message archival to MongoDB
- [ ] Cache strategy optimized

### User Experience
- [ ] Smooth animations
- [ ] Loading states shown
- [ ] Error messages friendly
- [ ] Preferences respected
- [ ] Offline mode graceful
- [ ] Mobile responsive

---

## Technology Stack

### Backend
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL
- **Cache:** Redis
- **ORM:** JPA/Hibernate
- **Validation:** Jakarta Validation
- **Build:** Maven
- **Language:** Java 21+

### Frontend
- **Framework:** React 18+
- **Router:** React Router v6
- **HTTP:** Axios
- **Markdown:** react-markdown
- **Build:** Vite
- **Language:** JavaScript/JSX

### AI/External
- **API:** Google Gemini
- **Archive Database:** MongoDB (future)
- **Deployment:** Docker + Kubernetes (future)

---

## Development Timeline

```
Week 1:
  Day 1: ✅ Database Schema & Core Models (COMPLETE)
  Day 2: Repository & Service Layer
  Day 3: AI Service Redesign
  Day 4: Frontend Chat UI
  Day 5: Preferences Panel + Engagement Widgets
  Day 6: Integration Testing
  Day 7: Polish & Deployment

Week 2+:
  - Bug fixes from user testing
  - Performance optimization
  - Advanced features (analytics, export, etc)
  - Mobile optimization
```

---

## Resources & References

### Database Design
- PostgreSQL Official Documentation: https://www.postgresql.org/docs/
- Database Design Best Practices: https://use-the-index-luke.com/

### Java Development
- Spring Boot Docs: https://spring.io/projects/spring-boot
- JPA/Hibernate Docs: https://hibernate.org/orm/documentation/
- Java Best Practices: https://www.oracle.com/java/technologies/

### Frontend
- React Documentation: https://react.dev/
- Vite Documentation: https://vitejs.dev/

### Design Patterns
- Microservices Patterns: https://microservices.io/
- Design Patterns in Java: https://refactoring.guru/design-patterns/java

---

## Next Steps

✅ **Day 1 Complete:** Foundation is set
⏳ **Ready for Day 2:** Start building repositories and services

When you're ready to continue, refer to this guide and the Day 1 documentation files for context.

---

**Document Version:** 1.0
**Last Updated:** November 7, 2025
**Status:** Foundation Complete, Ready for Day 2

This is production-grade architecture built with enterprise standards. Let's keep this momentum going! 🚀

