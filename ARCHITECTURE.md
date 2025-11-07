# SkillSync AI Chat System - Enterprise Architecture

## Overview

This document describes the enterprise-grade architecture for the SkillSync AI Chat system built with industry-standard patterns and practices.

---

## 1. Database Design Philosophy

### Enum Tables Pattern

Instead of using CHECK constraints for enums, we use separate tables. This provides:

✅ **Maintainability** - Add new status without schema migration
✅ **Queryability** - Easy filtering and reporting
✅ **Audit Trail** - Track when statuses are added
✅ **Foreign Key Integrity** - Database enforces valid values

**Tables:**
- `conversation_status` - (ACTIVE, ARCHIVED, DELETED, PAUSED)
- `message_role` - (USER, ASSISTANT, SYSTEM)
- `context_level` - (MINIMAL, BALANCED, DEEP)
- `response_style` - (CONCISE, BALANCED, DETAILED)
- `ai_tone` - (PROFESSIONAL, CASUAL, MENTORING)
- `plan_type` - (FREE, PREMIUM, PRO)

### Soft Deletes

All main tables include `deleted_at` column for soft deletes:

```sql
-- Data is never really deleted
UPDATE chat_conversations SET deleted_at = NOW() WHERE conversation_id = ?;

-- Queries automatically filter out deleted rows
SELECT * FROM chat_conversations WHERE deleted_at IS NULL;
```

**Benefits:**
- GDPR compliance (audit trails, recovery)
- Prevents accidental data loss
- Easy to "un-delete"

### Denormalization Strategy

We deliberately denormalize `message_count` and `last_message_at` on conversations:

```
chat_conversations:
  - message_count: INT (denormalized from chat_messages count)
  - last_message_at: TIMESTAMP (denormalized from max created_at)
```

**Why?** Prevents expensive COUNT queries when listing user's conversations.

### Indexing Strategy

Covering indexes on common queries:

```sql
-- List user's conversations sorted by date
CREATE INDEX idx_chat_conversations_user_created
  ON chat_conversations(user_id, created_at DESC)
  WHERE deleted_at IS NULL;

-- Get recent messages for a conversation
CREATE INDEX idx_chat_messages_conversation_created
  ON chat_messages(conversation_id, created_at DESC)
  WHERE deleted_at IS NULL;
```

---

## 2. Application Layer Architecture

### Design Patterns Used

#### 1. **Repository Pattern**
Isolates data access logic from business logic.

```
┌─────────────────────┐
│   Service Layer     │ (Business logic)
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│ Repository Layer    │ (Data access)
└──────────┬──────────┘
           │
           ↓
┌─────────────────────┐
│   JPA Entities      │ (Database)
└─────────────────────┘
```

**Benefit:** Easy to test, swap implementations (mock repo for unit tests)

#### 2. **Service Layer Pattern**
Contains all business logic, orchestrates repositories.

```java
@Service
public class ChatService {
    @Autowired
    private ChatConversationRepository conversationRepo;

    @Autowired
    private ChatMessageRepository messageRepo;

    public void sendMessage(UUID conversationId, ChatMessage message) {
        // Business logic here
        messageRepo.save(message);
        conversationRepo.updateLastMessage(conversationId, message);
    }
}
```

#### 3. **DTO Pattern**
Separates API contract from database schema.

```
User Request
    ↓
ChatMessageDTO (API input)
    ↓
Entity (Database)
    ↓
ChatMessageDTO (API output)
    ↓
JSON Response
```

**Benefits:**
- API evolves independently from DB
- Hide sensitive fields
- Validation at boundary

#### 4. **Enum Pattern**
Type-safe enums instead of string literals.

```java
// Bad
String status = "ACTIVE";  // Can be misspelled at runtime

// Good
ConversationStatus status = ConversationStatus.ACTIVE;  // Compile-time safety
```

### Dependency Injection

All services use constructor injection (not @Autowired on fields):

```java
@Service
public class ChatService {
    private final ChatConversationRepository conversationRepo;
    private final ChatMessageRepository messageRepo;

    // Constructor injection - better testability
    public ChatService(ChatConversationRepository conversationRepo,
                       ChatMessageRepository messageRepo) {
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
    }
}
```

**Benefits:**
- Immutable dependencies
- Easy to mock for testing
- Clear dependency graph

---

## 3. Error Handling Strategy

### Custom Exception Hierarchy

```
RuntimeException
├── SkillSyncException (Base custom exception)
│   ├── ChatNotFoundException
│   ├── UnauthorizedException
│   ├── QuotaExceededException
│   ├── GeminiAPIException
│   └── ... (others)
```

**Benefits:**
- Catch specific exceptions
- Return appropriate HTTP status
- Structured error responses

### Error Response Format

```json
{
  "error": {
    "code": "CONVERSATION_NOT_FOUND",
    "message": "Conversation with ID xyz not found",
    "details": "User doesn't have access to this conversation",
    "timestamp": "2025-11-07T12:34:56.000Z"
  }
}
```

### Global Exception Handler

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ChatNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("CONVERSATION_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(QuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handle(QuotaExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
            .body(new ErrorResponse("QUOTA_EXCEEDED", e.getMessage()));
    }
}
```

---

## 4. Caching Strategy

### Redis Caching Levels

#### Level 1: User Preferences
- Key: `user_prefs:{user_id}`
- TTL: 1 hour
- Hit rate: Very high (rarely changes)

#### Level 2: Conversation Context
- Key: `conversation_context:{conversation_id}`
- TTL: 5 minutes
- Hit rate: Medium (loaded on conversation load)

#### Level 3: AI Response Cache
- Key: `response_cache:{user_id}:{request_hash}`
- TTL: 24 hours
- Hit rate: Low (used only when Gemini fails)

### Cache Invalidation

```java
@CachePut(value = "user_preferences", key = "#userId")
public UserAIPreferences updatePreferences(UUID userId, PreferencesDTO dto) {
    // Update database
    // @CachePut automatically updates cache
}

// Manual cache eviction if needed
@CacheEvict(value = "conversation_context", key = "#conversationId")
public void archiveConversation(UUID conversationId) {
    // Archive conversation
}
```

---

## 5. API Design Principles

### REST Conventions

```
POST   /api/v1/conversations              - Create
GET    /api/v1/conversations              - List (paginated)
GET    /api/v1/conversations/:id          - Get one
PATCH  /api/v1/conversations/:id          - Update
DELETE /api/v1/conversations/:id          - Delete (soft)

POST   /api/v1/conversations/:id/messages - Add message
GET    /api/v1/conversations/:id/messages - List messages (paginated)

GET    /api/v1/preferences                - Get user preferences
PATCH  /api/v1/preferences                - Update preferences
POST   /api/v1/preferences/reset          - Reset to defaults
POST   /api/v1/preferences/apply-preset   - Apply preset
```

### Pagination

```json
GET /api/v1/conversations?page=1&size=20&sort=created_at:desc

Response:
{
  "data": [
    { "conversation_id": "...", ... }
  ],
  "pagination": {
    "page": 1,
    "size": 20,
    "total": 150,
    "total_pages": 8
  }
}
```

### Rate Limiting

```java
@RateLimiter(limit = 100, window = "1m")  // 100 requests per minute
public ResponseEntity<AIResponse> generateResponse(@PathVariable UUID conversationId) {
    // ...
}

@RateLimiter(limit = 1000, window = "1d") // 1000 requests per day
public ResponseEntity<AIResponse> generateResponse(@PathVariable UUID conversationId) {
    // ...
}
```

---

## 6. Security

### Authentication

```java
@Component
public class JwtTokenProvider {
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getUserId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact();
    }
}
```

### Authorization

```java
@PreAuthorize("hasRole('USER')")
@GetMapping("/api/v1/conversations")
public ResponseEntity<List<ChatConversationDTO>> getUserConversations() {
    UUID userId = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    // ...
}
```

### Input Validation

```java
@Data
public class CreateMessageRequest {
    @NotBlank(message = "Message content is required")
    @Size(min = 1, max = 10000, message = "Message must be between 1 and 10000 characters")
    private String content;
}
```

---

## 7. Monitoring & Observability

### Structured Logging

```java
logger.info("User created conversation",
    extra("user_id", userId),
    extra("conversation_id", conversationId),
    extra("title", title)
);

logger.warn("AI API rate limit approaching",
    extra("user_id", userId),
    extra("requests_used", requestsUsed),
    extra("requests_limit", requestsLimit)
);
```

### Metrics

```java
@Timed(value = "chat.message.response_time", description = "Time to generate AI response")
public AIResponse generateResponse(UUID conversationId, String question) {
    // ...
}

@Counted(value = "chat.messages.total", description = "Total messages sent")
public void sendMessage(ChatMessage message) {
    // ...
}
```

### Health Checks

```java
@Component
public class GeminiAPIHealthIndicator extends AbstractHealthIndicator {
    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            geminiService.checkHealth();
            builder.up().withDetail("status", "Gemini API is responding");
        } catch (Exception e) {
            builder.down().withDetail("error", e.getMessage());
        }
    }
}
```

---

## 8. Data Flow

### Message Send Flow

```
User sends message
    ↓
ValidateInput (ChatMessageValidator)
    ↓
CheckQuotas (ChatService.canSendMessage)
    ↓
SaveMessage (ChatMessageRepository)
    ↓
BuildContext (ContextBuilderService)
    ↓
CallGemini (AIService.generateResponse)
    ↓
SaveResponse (ChatMessageRepository)
    ↓
UpdateConversation (ChatConversationRepository)
    ↓
Return to user
    ↓
(On failure → Use cached response)
```

### Database Transaction Strategy

```java
@Transactional
public void sendMessage(UUID conversationId, String content) {
    // All these operations are atomic
    chatMessageRepository.save(userMessage);
    chatMessageRepository.save(aiResponse);
    chatConversationRepository.updateLastMessage(conversationId, aiResponse);
    chatSessionRepository.incrementMessageCount(userId);

    // If any fails, all rollback
}
```

---

## 9. Scalability Considerations

### Database Scaling

**Read Replicas:**
```properties
spring.datasource.master.url=jdbc:postgresql://primary-db:5432/skillsync
spring.datasource.replica.url=jdbc:postgresql://replica-db:5432/skillsync
```

**Write to primary, read from replica**

### API Server Scaling

```
Load Balancer
├── App Server 1 (stateless)
├── App Server 2 (stateless)
├── App Server 3 (stateless)
└── App Server 4 (stateless)

Shared Cache (Redis)
Shared Database (PostgreSQL)
Shared File Storage
```

### Message Queue for AI Calls

```
User sends message
    ↓
Store in DB
    ↓
Push to RabbitMQ queue
    ↓
Return "Processing" response to user
    ↓
AI worker picks up from queue
    ↓
Generates response
    ↓
Saves response
    ↓
WebSocket notification to user
```

---

## 10. File Structure

```
backend/skillsync-api/
├── src/main/java/com/skillsync/
│   ├── controller/
│   │   ├── ChatController.java
│   │   ├── ConversationController.java
│   │   └── PreferencesController.java
│   │
│   ├── service/
│   │   ├── ChatService.java
│   │   ├── PreferencesService.java
│   │   ├── ContextBuilderService.java
│   │   └── EngagementService.java
│   │
│   ├── repository/
│   │   ├── ChatConversationRepository.java
│   │   ├── ChatMessageRepository.java
│   │   └── UserAIPreferencesRepository.java
│   │
│   ├── entity/
│   │   ├── ChatConversation.java
│   │   ├── ChatMessage.java
│   │   └── UserAIPreferences.java
│   │
│   ├── dto/
│   │   ├── chat/
│   │   │   ├── ChatMessageDTO.java
│   │   │   └── ChatConversationDTO.java
│   │   ├── preferences/
│   │   │   └── UserAIPreferencesDTO.java
│   │   └── request/
│   │       ├── CreateMessageRequest.java
│   │       └── UpdatePreferencesRequest.java
│   │
│   ├── enums/
│   │   ├── ConversationStatus.java
│   │   ├── MessageRole.java
│   │   ├── ContextLevel.java
│   │   ├── ResponseStyle.java
│   │   ├── AITone.java
│   │   └── PlanType.java
│   │
│   ├── exception/
│   │   ├── SkillSyncException.java
│   │   ├── ChatNotFoundException.java
│   │   ├── UnauthorizedException.java
│   │   ├── QuotaExceededException.java
│   │   └── GlobalExceptionHandler.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── SecurityConfig.java
│   │   └── CustomUserDetailsService.java
│   │
│   └── util/
│       ├── CacheManager.java
│       ├── LoggingUtil.java
│       └── ValidationUtil.java
│
└── src/main/resources/
    ├── db/migration/
    │   └── V4__Create_Chat_System_Schema.sql
    └── application-prod.yml
```

---

## 11. Testing Strategy

### Unit Tests (Service Layer)

```java
@Test
public void testCreateConversation_Success() {
    // Arrange
    UUID userId = UUID.randomUUID();

    // Act
    ChatConversation conversation = chatService.createConversation(userId, "Test Chat");

    // Assert
    assertEquals(ConversationStatus.ACTIVE, conversation.getStatus());
    assertEquals(0, conversation.getMessageCount());
}

@Test
public void testCanCreateConversation_ExceedsFreeTierLimit() {
    // Arrange
    chatSessionRepository.setActive Conversations(userId, 2);

    // Act & Assert
    assertThrows(QuotaExceededException.class,
        () -> chatService.createConversation(userId, "Third Chat"));
}
```

### Integration Tests (API)

```java
@SpringBootTest
@AutoConfigureMockMvc
public class ChatControllerIntegrationTest {

    @Test
    public void testSendMessage_Success() throws Exception {
        mockMvc.perform(post("/api/v1/conversations/{id}/messages", conversationId)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(messageRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.role").value("ASSISTANT"));
    }
}
```

---

## 12. Deployment Checklist

- [ ] Database migrations applied
- [ ] Redis cache configured
- [ ] Environment variables set
- [ ] Gemini API key configured
- [ ] Logging configured
- [ ] Monitoring/alerting configured
- [ ] Rate limiting configured
- [ ] Security headers configured
- [ ] CORS configured
- [ ] Load balancer configured
- [ ] SSL/TLS certificates installed
- [ ] Database backups configured
- [ ] Health checks verified
- [ ] Load tests completed

---

## 13. References & Resources

- **JPA Best Practices**: https://hibernate.org/orm/documentation/
- **Spring Data**: https://spring.io/projects/spring-data
- **Microservices Patterns**: https://microservices.io/
- **API Design**: https://restfulapi.net/
- **Database Design**: https://use-the-index-luke.com/

---

**Last Updated**: November 7, 2025
**Version**: 1.0 - Enterprise Grade
**Team**: SkillSync Development
