package com.skillsync.repository;

import com.skillsync.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ChatMessageRepository
 * Spring Data JPA repository for ChatMessage entity
 *
 * Provides:
 * - CRUD operations for messages
 * - Pagination for message lists
 * - Search and filtering by role, content, date
 * - Bulk operations for token tracking
 * - Caching support via is_cached flag
 *
 * Design Pattern: Repository Pattern
 * Isolates message data access from business logic
 *
 * Performance Strategy:
 * - Only hot data (recent messages) stored in PostgreSQL
 * - Older messages archived to MongoDB
 * - Covering indexes on conversation_id and user_id
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Get all messages in a conversation (not deleted)
     * Most common query - optimized with covering index
     */
    Page<ChatMessage> findByConversationIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        UUID conversationId,
        Pageable pageable
    );

    /**
     * Get paginated messages sorted ascending (newest last)
     * Used for rendering chat UI (scroll to bottom)
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt ASC")
    Page<ChatMessage> findConversationMessagesAscending(
        @Param("conversationId") UUID conversationId,
        Pageable pageable
    );

    /**
     * Get messages for a user across all conversations
     * Used for user activity/audit logs
     */
    Page<ChatMessage> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        UUID userId,
        Pageable pageable
    );

    /**
     * Get a single message by ID
     * Validates existence and returns Optional
     */
    Optional<ChatMessage> findByMessageIdAndDeletedAtIsNull(UUID messageId);

    /**
     * Get last N messages in a conversation (for AI context)
     * These are the messages sent to Gemini for context
     * Limit: 10-20 messages to stay within token budget
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT :limit")
    List<ChatMessage> findLastMessagesInConversation(
        @Param("conversationId") UUID conversationId,
        @Param("limit") int limit
    );

    /**
     * Get all user messages in a conversation (for history context)
     * Excludes ASSISTANT and SYSTEM messages
     * Used when building context for AI
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.roleId = :userRoleId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> findUserMessagesInConversation(
        @Param("conversationId") UUID conversationId,
        @Param("userRoleId") Short userRoleId
    );

    /**
     * Get all assistant responses in a conversation
     * Used for context building and quality analysis
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.roleId = :assistantRoleId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessage> findAssistantMessagesInConversation(
        @Param("conversationId") UUID conversationId,
        @Param("assistantRoleId") Short assistantRoleId
    );

    /**
     * Get most recent message in conversation
     * Used for getting the latest response
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT 1")
    Optional<ChatMessage> findLatestMessage(@Param("conversationId") UUID conversationId);

    /**
     * Get most recent assistant message (AI response)
     * Used when falling back to previous response
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.roleId = :assistantRoleId " +
           "AND m.deletedAt IS NULL " +
           "AND m.isCached = false " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT 1")
    Optional<ChatMessage> findMostRecentResponse(
        @Param("conversationId") UUID conversationId,
        @Param("assistantRoleId") Short assistantRoleId
    );

    /**
     * Search messages by content (keyword search)
     * Case-insensitive search across message content
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL " +
           "AND LOWER(m.content) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessage> searchMessages(
        @Param("conversationId") UUID conversationId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    /**
     * Count messages in a conversation
     * Used for pagination and UI display
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL")
    Long countMessagesInConversation(@Param("conversationId") UUID conversationId);

    /**
     * Count all user messages across all conversations
     * Used for usage analytics
     */
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "WHERE m.userId = :userId " +
           "AND m.deletedAt IS NULL")
    Long countTotalUserMessages(@Param("userId") UUID userId);

    /**
     * Sum token counts for cost estimation
     * Used for billing and quota management
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL")
    Long sumTokensInConversation(@Param("conversationId") UUID conversationId);

    /**
     * Sum token counts across all user messages
     * Used for daily/monthly quota tracking
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m " +
           "WHERE m.userId = :userId " +
           "AND m.deletedAt IS NULL " +
           "AND m.createdAt > :sinceTime")
    Long sumTokensSinceTime(
        @Param("userId") UUID userId,
        @Param("sinceTime") LocalDateTime sinceTime
    );

    /**
     * Get messages with caching enabled (fallback responses)
     * Used when Gemini API is down
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.isCached = true " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC " +
           "LIMIT 1")
    Optional<ChatMessage> findCachedResponse(@Param("conversationId") UUID conversationId);

    /**
     * Get messages by finish reason (for debugging Gemini issues)
     * Helps identify blocked responses, truncations, etc.
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.userId = :userId " +
           "AND m.geminiFinishReason = :finishReason " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC")
    Page<ChatMessage> findMessagesByFinishReason(
        @Param("userId") UUID userId,
        @Param("finishReason") String finishReason,
        Pageable pageable
    );

    /**
     * Bulk update token counts
     * Called after tokens are estimated for a message
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m " +
           "SET m.tokenCount = :tokenCount " +
           "WHERE m.messageId = :messageId")
    void updateTokenCount(
        @Param("messageId") UUID messageId,
        @Param("tokenCount") Integer tokenCount
    );

    /**
     * Update message as cached response
     * Called when using fallback response
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m " +
           "SET m.isCached = true " +
           "WHERE m.messageId = :messageId")
    void markAsCached(@Param("messageId") UUID messageId);

    /**
     * Update Gemini finish reason for debugging
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m " +
           "SET m.geminiFinishReason = :finishReason " +
           "WHERE m.messageId = :messageId")
    void updateFinishReason(
        @Param("messageId") UUID messageId,
        @Param("finishReason") String finishReason
    );

    /**
     * Soft delete a message
     * Used when user deletes a message
     */
    @Modifying
    @Transactional
    void deleteByMessageId(UUID messageId);

    /**
     * Soft delete all messages in a conversation
     * Called when conversation is deleted
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m " +
           "SET m.deletedAt = :deletedAt " +
           "WHERE m.conversationId = :conversationId")
    void softDeleteConversationMessages(
        @Param("conversationId") UUID conversationId,
        @Param("deletedAt") LocalDateTime deletedAt
    );

    /**
     * Archive old messages to MongoDB
     * Gets messages older than specified date (for archival)
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.createdAt < :archiveThreshold " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt DESC")
    List<ChatMessage> findMessagesForArchival(
        @Param("archiveThreshold") LocalDateTime archiveThreshold
    );

    /**
     * Check if user owns this message
     * Authorization check
     */
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM ChatMessage m " +
           "WHERE m.messageId = :messageId " +
           "AND m.userId = :userId")
    boolean isMessageOwner(
        @Param("messageId") UUID messageId,
        @Param("userId") UUID userId
    );

    /**
     * Export conversation messages
     * Used for backup/export features
     */
    @Query("SELECT m FROM ChatMessage m " +
           "WHERE m.conversationId = :conversationId " +
           "AND m.deletedAt IS NULL " +
           "ORDER BY m.createdAt ASC")
    List<ChatMessage> exportConversationMessages(@Param("conversationId") UUID conversationId);
}
