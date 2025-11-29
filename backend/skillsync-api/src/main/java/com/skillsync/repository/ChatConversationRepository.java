package com.skillsync.repository;

import com.skillsync.entity.ChatConversation;
import com.skillsync.enums.ConversationStatus;
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
 * ChatConversationRepository
 * Spring Data JPA repository for ChatConversation entity
 *
 * Provides:
 * - CRUD operations via JpaRepository
 * - Custom queries for common use cases
 * - Pagination and sorting support
 * - Bulk operations for performance
 *
 * Design Pattern: Repository Pattern
 * Isolates data access logic from business logic
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    /**
     * Get all active conversations for a user (not deleted)
     * Most common query - optimized with partial index in DB
     */
    Page<ChatConversation> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(
        UUID userId,
        Pageable pageable
    );

    /**
     * Get all conversations for a user, regardless of status
     * Used for admin/archival operations
     */
    Page<ChatConversation> findByUserIdOrderByCreatedAtDesc(
        UUID userId,
        Pageable pageable
    );

    /**
     * Get a single conversation by ID and user ID
     * Ensures user only accesses their own conversations
     */
    Optional<ChatConversation> findByConversationIdAndUserId(
        UUID conversationId,
        UUID userId
    );

    /**
     * Get an active conversation (not deleted)
     * Custom query to ensure we don't return soft-deleted conversations
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.conversationId = :conversationId " +
           "AND c.userId = :userId " +
           "AND c.deletedAt IS NULL")
    Optional<ChatConversation> findActiveConversation(
        @Param("conversationId") UUID conversationId,
        @Param("userId") UUID userId
    );

    /**
     * Count active conversations for a user
     * Used for quota checking (free tier: 2 conversations)
     */
    @Query("SELECT COUNT(c) FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND c.statusId != :archivedStatusId")
    Long countActiveConversations(
        @Param("userId") UUID userId,
        @Param("archivedStatusId") Short archivedStatusId
    );

    /**
     * Count only conversations with ACTIVE status for a user
     * Stricter quota check - exclude ARCHIVED and PAUSED
     */
    @Query("SELECT COUNT(c) FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND c.statusId = :activeStatusId")
    Long countConversationsWithStatus(
        @Param("userId") UUID userId,
        @Param("activeStatusId") Short activeStatusId
    );

    /**
     * Get user's most recent conversations (for UI sidebar/list)
     * Limits to 10 most recent active conversations
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND c.statusId = :activeStatusId " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST " +
           "LIMIT 10")
    List<ChatConversation> findRecentActiveConversations(
        @Param("userId") UUID userId,
        @Param("activeStatusId") Short activeStatusId
    );

    /**
     * Get conversations that need summary refresh
     * Summary is refreshed every 20 messages or hourly
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND (c.summaryLastUpdated IS NULL " +
           "OR c.summaryLastUpdated < :refreshThreshold)")
    List<ChatConversation> findConversationsNeedingSummary(
        @Param("userId") UUID userId,
        @Param("refreshThreshold") LocalDateTime refreshThreshold
    );

    /**
     * Get conversations last updated before a certain time
     * Used for archival and cleanup operations
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND c.lastMessageAt < :beforeTime")
    Page<ChatConversation> findInactiveConversations(
        @Param("userId") UUID userId,
        @Param("beforeTime") LocalDateTime beforeTime,
        Pageable pageable
    );

    /**
     * Bulk update last message info
     * Called when a new message is added to conversation
     * Very efficient - single SQL UPDATE
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatConversation c " +
           "SET c.lastMessageId = :messageId, " +
           "c.lastMessageAt = :timestamp, " +
           "c.messageCount = c.messageCount + 1 " +
           "WHERE c.conversationId = :conversationId")
    void updateLastMessage(
        @Param("conversationId") UUID conversationId,
        @Param("messageId") UUID messageId,
        @Param("timestamp") LocalDateTime timestamp
    );

    /**
     * Update conversation summary and timestamp
     * Called after generating AI summary
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatConversation c " +
           "SET c.conversationSummary = :summary, " +
           "c.summaryLastUpdated = :timestamp " +
           "WHERE c.conversationId = :conversationId")
    void updateSummary(
        @Param("conversationId") UUID conversationId,
        @Param("summary") String summary,
        @Param("timestamp") LocalDateTime timestamp
    );

    /**
     * Update conversation status
     * Used for archiving, pausing, etc.
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatConversation c " +
           "SET c.statusId = :statusId " +
           "WHERE c.conversationId = :conversationId")
    void updateStatus(
        @Param("conversationId") UUID conversationId,
        @Param("statusId") Short statusId
    );

    /**
     * Soft delete a conversation
     * Sets deleted_at timestamp instead of actually deleting
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatConversation c " +
           "SET c.deletedAt = :deletedAt, " +
           "c.statusId = :deletedStatusId " +
           "WHERE c.conversationId = :conversationId")
    void softDelete(
        @Param("conversationId") UUID conversationId,
        @Param("deletedAt") LocalDateTime deletedAt,
        @Param("deletedStatusId") Short deletedStatusId
    );

    /**
     * Hard delete (for testing/admin only - use with caution!)
     * This permanently deletes the conversation
     */
    @Modifying
    @Transactional
    void deleteByConversationIdAndUserId(UUID conversationId, UUID userId);

    /**
     * Check if user owns this conversation
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
           "FROM ChatConversation c " +
           "WHERE c.conversationId = :conversationId " +
           "AND c.userId = :userId")
    boolean isConversationOwner(
        @Param("conversationId") UUID conversationId,
        @Param("userId") UUID userId
    );

    /**
     * Search conversations by title
     * Used for conversation search functionality
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "AND LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ChatConversation> searchConversations(
        @Param("userId") UUID userId,
        @Param("query") String query,
        Pageable pageable
    );

    /**
     * Export user's conversations with message counts
     * Used for data export/backup features
     */
    @Query("SELECT c FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL " +
           "ORDER BY c.createdAt DESC")
    List<ChatConversation> exportUserConversations(@Param("userId") UUID userId);
}
