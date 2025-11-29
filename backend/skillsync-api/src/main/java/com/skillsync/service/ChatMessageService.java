package com.skillsync.service;

import com.skillsync.dto.chat.ChatMessageDTO;
import com.skillsync.entity.ChatMessage;
import com.skillsync.enums.MessageRole;
import com.skillsync.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ChatMessageService
 * Manages chat messages within conversations
 *
 * Responsibilities:
 * - Create and store user messages
 * - Create and store AI responses
 * - Retrieve conversation history
 * - Track token usage for cost estimation
 * - Handle message caching status
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;

    /**
     * Save a user message to the database
     */
    @Transactional
    public ChatMessageDTO saveUserMessage(UUID conversationId, UUID userId, String content) {
        log.debug("Saving user message to conversation: {}", conversationId);

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(MessageRole.USER)
                .content(content)
                .tokenCount(estimateTokenCount(content))
                .isCached(false)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage saved = messageRepository.save(message);
        log.debug("User message saved with ID: {}", saved.getMessageId());

        return ChatMessageDTO.fromEntity(saved);
    }

    /**
     * Save an assistant (AI) message to the database
     */
    @Transactional
    public ChatMessageDTO saveAssistantMessage(UUID conversationId, UUID userId, String content, Integer tokenCount, Boolean isCached) {
        log.debug("Saving assistant message to conversation: {}", conversationId);

        ChatMessage message = ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(MessageRole.ASSISTANT)
                .content(content)
                .tokenCount(tokenCount != null ? tokenCount : estimateTokenCount(content))
                .isCached(isCached != null && isCached)
                .createdAt(LocalDateTime.now())
                .build();

        ChatMessage saved = messageRepository.save(message);
        log.debug("Assistant message saved with ID: {}", saved.getMessageId());

        return ChatMessageDTO.fromEntity(saved);
    }

    /**
     * Get all messages in a conversation (paginated)
     */
    @Transactional(readOnly = true)
    public List<ChatMessageDTO> getConversationMessages(UUID conversationId, int page, int size) {
        log.debug("Fetching messages for conversation: {} (page {}, size {})", conversationId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        var pageResult = messageRepository.findConversationMessagesAscending(conversationId, pageable);

        return pageResult.stream()
                .map(ChatMessageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get recent messages for context building (last N messages)
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getRecentMessagesForContext(UUID conversationId, int limit) {
        log.debug("Fetching last {} messages from conversation: {} for context", limit, conversationId);
        return messageRepository.findLastMessagesInConversation(conversationId, limit);
    }

    /**
     * Get last message in a conversation (for checking if it's from user or assistant)
     */
    @Transactional(readOnly = true)
    public Optional<ChatMessage> getLastMessage(UUID conversationId) {
        log.debug("Fetching last message from conversation: {}", conversationId);
        return messageRepository.findLatestMessage(conversationId);
    }

    /**
     * Count messages in a conversation
     */
    @Transactional(readOnly = true)
    public int getMessageCount(UUID conversationId) {
        long count = messageRepository.countMessagesInConversation(conversationId);
        return (int) count;
    }

    /**
     * Get total token count for a conversation (for billing/quota)
     */
    @Transactional(readOnly = true)
    public int getTotalTokenCount(UUID conversationId) {
        Long sum = messageRepository.sumTokensInConversation(conversationId);
        return sum != null ? sum.intValue() : 0;
    }

    /**
     * Delete a message (soft delete)
     */
    @Transactional
    public void deleteMessage(UUID messageId) {
        log.info("Deleting message: {}", messageId);
        messageRepository.deleteByMessageId(messageId);
    }

    /**
     * Estimate token count for content
     * Rough estimate: ~1 token per 4 characters (OpenAI's approximation)
     */
    private int estimateTokenCount(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        return Math.max(1, content.length() / 4);
    }

    /**
     * Get conversation history as ChatMessage entities
     * Excludes deleted messages
     */
    @Transactional(readOnly = true)
    public List<ChatMessage> getConversationHistory(UUID conversationId) {
        return messageRepository.exportConversationMessages(conversationId);
    }

    /**
     * Get cached response for fallback when Gemini is down
     */
    @Transactional(readOnly = true)
    public Optional<ChatMessage> getCachedResponse(UUID conversationId) {
        log.debug("Fetching cached response for conversation: {}", conversationId);
        return messageRepository.findCachedResponse(conversationId);
    }

    /**
     * Mark a message as cached for fallback purposes
     */
    @Transactional
    public void markAsCached(UUID messageId) {
        log.debug("Marking message {} as cached", messageId);
        messageRepository.markAsCached(messageId);
    }

    /**
     * Update token count for a message
     */
    @Transactional
    public void updateTokenCount(UUID messageId, Integer tokenCount) {
        log.debug("Updating token count for message: {}", messageId);
        messageRepository.updateTokenCount(messageId, tokenCount);
    }
}
