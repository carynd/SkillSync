package com.skillsync.controller;

import com.skillsync.dto.chat.ChatConversationDTO;
import com.skillsync.dto.chat.ChatMessageDTO;
import com.skillsync.entity.ChatMessage;
import com.skillsync.entity.UserAIPreferences;
import com.skillsync.model.User;
import com.skillsync.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ChatController
 * REST API endpoints for chat conversations and messages
 *
 * Endpoints:
 * - POST /api/chat/conversations - Create conversation
 * - GET /api/chat/conversations - List user's conversations
 * - GET /api/chat/conversations/{id} - Get specific conversation
 * - POST /api/chat/conversations/{id}/messages - Send message
 * - GET /api/chat/conversations/{id}/messages - Get messages
 * - GET /api/chat/quota - Check conversation quota
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatConversationService conversationService;
    private final ChatMessageService messageService;
    private final AIPreferencesService preferencesService;
    private final AIContextBuilder contextBuilder;
    private final GeminiAIService geminiAIService;

    /**
     * Create a new conversation
     */
    @PostMapping("/conversations")
    public ResponseEntity<ChatConversationDTO> createConversation(
            @RequestParam(required = false) String title,
            @RequestAttribute("userId") UUID userId) {
        log.info("Creating conversation for user: {}", userId);

        try {
            ChatConversationDTO conversation = conversationService.createConversation(userId, title);
            return ResponseEntity.status(HttpStatus.CREATED).body(conversation);
        } catch (IllegalStateException e) {
            log.warn("Cannot create conversation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Get all conversations for user
     */
    @GetMapping("/conversations")
    public ResponseEntity<Page<ChatConversationDTO>> getUserConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestAttribute("userId") UUID userId) {
        log.debug("Fetching conversations for user: {}", userId);

        Page<ChatConversationDTO> conversations = conversationService.getUserConversations(userId, page, size);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Get recent conversations (sidebar list)
     */
    @GetMapping("/conversations/recent")
    public ResponseEntity<List<ChatConversationDTO>> getRecentConversations(
            @RequestAttribute("userId") UUID userId) {
        log.debug("Fetching recent conversations for user: {}", userId);

        List<ChatConversationDTO> conversations = conversationService
                .getRecentConversations(userId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());

        return ResponseEntity.ok(conversations);
    }

    /**
     * Get a specific conversation with recent messages
     */
    @GetMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversationDTO> getConversation(
            @PathVariable UUID conversationId,
            @RequestAttribute("userId") UUID userId) {
        log.debug("Fetching conversation: {} for user: {}", conversationId, userId);

        try {
            ChatConversationDTO conversation = conversationService.getConversation(conversationId, userId);
            return ResponseEntity.ok(conversation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get messages in a conversation (paginated)
     */
    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> getConversationMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") UUID userId) {
        log.debug("Fetching messages for conversation: {}", conversationId);

        try {
            // Verify user owns this conversation
            conversationService.getConversation(conversationId, userId);

            List<ChatMessageDTO> messages = messageService.getConversationMessages(conversationId, page, size);

            Map<String, Object> response = new HashMap<>();
            response.put("conversation_id", conversationId);
            response.put("messages", messages);
            response.put("message_count", messageService.getMessageCount(conversationId));

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Send a message to a conversation
     * Calls AI service to get response with graceful fallback
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<Map<String, Object>> sendMessage(
            @PathVariable UUID conversationId,
            @RequestBody ChatMessageRequest request,
            @RequestAttribute("userId") UUID userId) {
        log.info("Sending message to conversation: {}", conversationId);

        try {
            // Verify user owns this conversation
            ChatConversationDTO conversation = conversationService.getConversation(conversationId, userId);

            // Validate message content
            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            // Save user message
            ChatMessageDTO userMessage = messageService.saveUserMessage(conversationId, userId, request.getContent());
            log.debug("User message saved: {}", userMessage.getMessageId());

            // Update conversation last message
            conversationService.updateLastMessage(conversationId, userMessage.getMessageId());

            // Build context for AI
            List<ChatMessage> recentMessages = messageService.getRecentMessagesForContext(conversationId, 10);
            UserAIPreferences prefs = preferencesService.getUserPreferencesEntity(userId);

            // Build rich context respecting user preferences
            String context = contextBuilder.buildContext(null, prefs, recentMessages);

            // Get AI response (with graceful fallback if unavailable)
            GeminiAIService.GeminiResponse aiResponse = geminiAIService.askGemini(
                    conversationId.toString(),
                    request.getContent(),
                    context,
                    prefs
            );

            // Save AI response to database
            ChatMessageDTO assistantMessage = messageService.saveAssistantMessage(
                    conversationId,
                    userId,
                    aiResponse.getContent(),
                    aiResponse.getTokenCount(),
                    aiResponse.isCached()
            );

            // Update conversation with last AI message
            conversationService.updateLastMessage(conversationId, assistantMessage.getMessageId());

            // Record preference usage for analytics
            preferencesService.recordPreferenceUsage(userId);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("user_message", userMessage);
            response.put("assistant_message", assistantMessage);
            response.put("success", aiResponse.isSuccess());
            response.put("is_cached", aiResponse.isCached());
            response.put("finish_reason", aiResponse.getFinishReason());
            if (aiResponse.getErrorMessage() != null) {
                response.put("warning", aiResponse.getErrorMessage());
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("Conversation not found or unauthorized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Unexpected error processing message", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update conversation title
     */
    @PatchMapping("/conversations/{conversationId}")
    public ResponseEntity<ChatConversationDTO> updateConversation(
            @PathVariable UUID conversationId,
            @RequestBody UpdateConversationRequest request,
            @RequestAttribute("userId") UUID userId) {
        log.info("Updating conversation: {}", conversationId);

        try {
            if (request.getTitle() != null) {
                return ResponseEntity.ok(conversationService.updateTitle(conversationId, userId, request.getTitle()));
            }
            return ResponseEntity.ok(conversationService.getConversation(conversationId, userId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Archive a conversation
     */
    @PostMapping("/conversations/{conversationId}/archive")
    public ResponseEntity<?> archiveConversation(
            @PathVariable UUID conversationId,
            @RequestAttribute("userId") UUID userId) {
        log.info("Archiving conversation: {}", conversationId);

        try {
            conversationService.archiveConversation(conversationId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Delete a conversation
     */
    @DeleteMapping("/conversations/{conversationId}")
    public ResponseEntity<?> deleteConversation(
            @PathVariable UUID conversationId,
            @RequestAttribute("userId") UUID userId) {
        log.info("Deleting conversation: {}", conversationId);

        try {
            conversationService.deleteConversation(conversationId, userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Get conversation quota info
     */
    @GetMapping("/quota")
    public ResponseEntity<ChatConversationService.ConversationQuotaInfo> getQuotaInfo(
            @RequestAttribute("userId") UUID userId) {
        log.debug("Getting quota info for user: {}", userId);

        ChatConversationService.ConversationQuotaInfo quotaInfo = conversationService.getQuotaInfo(userId);
        return ResponseEntity.ok(quotaInfo);
    }

    /**
     * Request DTO for sending messages
     */
    @lombok.Data
    public static class ChatMessageRequest {
        private String content;
    }

    /**
     * Request DTO for updating conversation
     */
    @lombok.Data
    public static class UpdateConversationRequest {
        private String title;
    }
}
