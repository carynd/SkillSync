package com.skillsync.service;

import com.skillsync.dto.chat.ChatConversationDTO;
import com.skillsync.entity.ChatConversation;
import com.skillsync.entity.ChatSession;
import com.skillsync.enums.ConversationStatus;
import com.skillsync.enums.PlanType;
import com.skillsync.repository.ChatConversationRepository;
import com.skillsync.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
 * ChatConversationService
 * Manages chat conversations for users
 *
 * Responsibilities:
 * - Create conversations with quota checking
 * - List user's conversations
 * - Retrieve specific conversation details
 * - Archive/pause/delete conversations
 * - Update conversation metadata (title, summary)
 * - Enforce conversation limits based on plan tier
 *
 * Quota System:
 * - FREE: 2 active conversations
 * - PREMIUM: 10 active conversations
 * - PRO: Unlimited conversations
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConversationService {

    private final ChatConversationRepository conversationRepository;
    private final ChatSessionRepository sessionRepository;

    /**
     * Create a new conversation for a user
     * Checks quota limits based on user's plan
     *
     * @throws IllegalStateException if user has exceeded conversation limit
     */
    @Transactional
    public ChatConversationDTO createConversation(UUID userId, String title) {
        log.info("Creating new conversation for user: {} with title: {}", userId, title);

        // Check quota limit
        if (!canCreateNewConversation(userId)) {
            log.warn("User {} has exceeded conversation quota", userId);
            throw new IllegalStateException("You have reached the maximum number of conversations for your plan");
        }

        ChatConversation conversation = ChatConversation.builder()
                .conversationId(UUID.randomUUID())
                .userId(userId)
                .title(title != null ? title : "New Conversation")
                .description(null)
                .status(ConversationStatus.ACTIVE)
                .statusId(ConversationStatus.ACTIVE.getDatabaseId())
                .messageCount(0)
                .lastMessageAt(null)
                .conversationSummary(null)
                .summaryLastUpdated(null)
                .createdAt(LocalDateTime.now())
                .build();

        ChatConversation saved = conversationRepository.save(conversation);
        log.info("Conversation created with ID: {}", saved.getConversationId());

        return ChatConversationDTO.fromEntity(saved);
    }

    /**
     * Get a specific conversation by ID (with authorization check)
     */
    @Transactional(readOnly = true)
    public ChatConversationDTO getConversation(UUID conversationId, UUID userId) {
        log.debug("Fetching conversation: {} for user: {}", conversationId, userId);

        ChatConversation conversation = conversationRepository
                .findActiveConversation(conversationId, userId)
                .orElseThrow(() -> {
                    log.warn("Conversation {} not found or user {} is not authorized", conversationId, userId);
                    return new IllegalArgumentException("Conversation not found");
                });

        return ChatConversationDTO.fromEntity(conversation);
    }

    /**
     * Get paginated list of user's conversations
     */
    @Transactional(readOnly = true)
    public Page<ChatConversationDTO> getUserConversations(UUID userId, int page, int size) {
        log.debug("Fetching conversations for user: {} (page {}, size {})", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatConversation> conversationsPage = conversationRepository
                .findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId, pageable);

        return conversationsPage.map(ChatConversationDTO::fromEntity);
    }

    /**
     * Get user's recent active conversations (sidebar list)
     */
    @Transactional(readOnly = true)
    public List<ChatConversationDTO> getRecentConversations(UUID userId) {
        log.debug("Fetching recent conversations for user: {}", userId);

        List<ChatConversation> conversations = conversationRepository
                .findRecentActiveConversations(userId, ConversationStatus.ACTIVE.getDatabaseId());

        return conversations.stream()
                .map(ChatConversationDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update conversation title
     */
    @Transactional
    public ChatConversationDTO updateTitle(UUID conversationId, UUID userId, String newTitle) {
        log.info("Updating title for conversation: {}", conversationId);

        ChatConversation conversation = conversationRepository
                .findActiveConversation(conversationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Conversation not found"));

        conversation.setTitle(newTitle);
        ChatConversation updated = conversationRepository.save(conversation);

        return ChatConversationDTO.fromEntity(updated);
    }

    /**
     * Archive a conversation (soft delete, make it ARCHIVED)
     */
    @Transactional
    public void archiveConversation(UUID conversationId, UUID userId) {
        log.info("Archiving conversation: {} for user: {}", conversationId, userId);

        if (!conversationRepository.isConversationOwner(conversationId, userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        conversationRepository.updateStatus(conversationId, ConversationStatus.ARCHIVED.getDatabaseId());
    }

    /**
     * Delete a conversation (soft delete with deleted_at timestamp)
     */
    @Transactional
    public void deleteConversation(UUID conversationId, UUID userId) {
        log.info("Deleting conversation: {} for user: {}", conversationId, userId);

        if (!conversationRepository.isConversationOwner(conversationId, userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        conversationRepository.softDelete(conversationId, LocalDateTime.now(), ConversationStatus.DELETED.getDatabaseId());
    }

    /**
     * Pause a conversation (user wants to take a break)
     */
    @Transactional
    public void pauseConversation(UUID conversationId, UUID userId) {
        log.info("Pausing conversation: {} for user: {}", conversationId, userId);

        if (!conversationRepository.isConversationOwner(conversationId, userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        conversationRepository.updateStatus(conversationId, ConversationStatus.PAUSED.getDatabaseId());
    }

    /**
     * Resume a paused conversation
     */
    @Transactional
    public void resumeConversation(UUID conversationId, UUID userId) {
        log.info("Resuming conversation: {} for user: {}", conversationId, userId);

        if (!conversationRepository.isConversationOwner(conversationId, userId)) {
            throw new IllegalArgumentException("Unauthorized");
        }

        conversationRepository.updateStatus(conversationId, ConversationStatus.ACTIVE.getDatabaseId());
    }

    /**
     * Update conversation summary (called by summarization service)
     */
    @Transactional
    public void updateConversationSummary(UUID conversationId, String summary) {
        log.debug("Updating summary for conversation: {}", conversationId);
        conversationRepository.updateSummary(conversationId, summary, LocalDateTime.now());
    }

    /**
     * Update last message info (called when message is added)
     */
    @Transactional
    public void updateLastMessage(UUID conversationId, UUID messageId) {
        log.debug("Updating last message for conversation: {}", conversationId);
        conversationRepository.updateLastMessage(conversationId, messageId, LocalDateTime.now());
    }

    /**
     * Check if user can create a new conversation
     * Based on their plan tier
     */
    @Transactional(readOnly = true)
    public boolean canCreateNewConversation(UUID userId) {
        log.debug("Checking if user {} can create new conversation", userId);

        ChatSession session = sessionRepository.findByUserId(userId)
                .orElse(null);

        if (session == null) {
            // New user defaults to FREE tier (2 conversations)
            long activeCount = conversationRepository.countConversationsWithStatus(userId, ConversationStatus.ACTIVE.getDatabaseId());
            return activeCount < 2;
        }

        PlanType planType = session.getPlanType();
        int maxConversations = session.getMaxConversationsAllowed();

        long activeCount = conversationRepository.countConversationsWithStatus(userId, ConversationStatus.ACTIVE.getDatabaseId());

        boolean canCreate = activeCount < maxConversations;
        log.debug("User {} plan: {}, active: {}, max: {}, can_create: {}",
                userId, planType, activeCount, maxConversations, canCreate);

        return canCreate;
    }

    /**
     * Get conversation quota info for a user
     */
    @Transactional(readOnly = true)
    public ConversationQuotaInfo getQuotaInfo(UUID userId) {
        log.debug("Getting quota info for user: {}", userId);

        ChatSession session = sessionRepository.findByUserId(userId)
                .orElse(null);

        PlanType planType = session != null ? session.getPlanType() : PlanType.FREE;
        int maxConversations = session != null ? session.getMaxConversationsAllowed() : 2;

        long activeCount = conversationRepository.countConversationsWithStatus(userId, ConversationStatus.ACTIVE.getDatabaseId());

        return ConversationQuotaInfo.builder()
                .planType(planType)
                .maxConversations(maxConversations)
                .activeConversations((int) activeCount)
                .remainingConversations(Math.max(0, maxConversations - (int) activeCount))
                .canCreateNew(activeCount < maxConversations)
                .build();
    }

    /**
     * Search conversations by title
     */
    @Transactional(readOnly = true)
    public Page<ChatConversationDTO> searchConversations(UUID userId, String query, int page, int size) {
        log.debug("Searching conversations for user: {} with query: {}", userId, query);

        Pageable pageable = PageRequest.of(page, size);
        Page<ChatConversation> results = conversationRepository.searchConversations(userId, query, pageable);

        return results.map(ChatConversationDTO::fromEntity);
    }

    /**
     * DTO for quota information
     */
    @lombok.Data
    @lombok.Builder
    public static class ConversationQuotaInfo {
        private PlanType planType;
        private int maxConversations;
        private int activeConversations;
        private int remainingConversations;
        private boolean canCreateNew;
    }
}
