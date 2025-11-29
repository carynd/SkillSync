package com.skillsync.repository;

import com.skillsync.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * ChatSessionRepository
 * Spring Data JPA repository for ChatSession entity
 *
 * Manages quota and plan tracking for users:
 * - FREE: 2 concurrent conversations
 * - PREMIUM: 10 concurrent conversations
 * - PRO: Unlimited conversations
 *
 * Provides:
 * - Quota checking and enforcement
 * - Plan type tracking
 * - Message count tracking for analytics
 * - Plan upgrade/downgrade operations
 * - Quota reset operations (monthly/yearly)
 *
 * Design Pattern: Repository Pattern
 * Isolates quota/plan data access from business logic
 *
 * Purpose:
 * Ensures monetization constraints and tracks usage for billing
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    /**
     * Get the current session for a user
     * Each user has one active session tracking their plan and quota
     *
     * @param userId The user's ID
     * @return Optional containing the session if it exists
     */
    Optional<ChatSession> findByUserId(UUID userId);

    /**
     * Get session for a user with a specific plan type
     * Used for filtering or analytics
     */
    Optional<ChatSession> findByUserIdAndPlanId(UUID userId, Short planId);

    /**
     * Check if user exists in session table
     * Tells us if user has a plan assigned
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
           "FROM ChatSession s " +
           "WHERE s.userId = :userId")
    boolean existsForUser(@Param("userId") UUID userId);

    /**
     * Get user's current plan type
     * Used for authorization checks (e.g., can user create 3rd conversation?)
     */
    @Query("SELECT s.planId FROM ChatSession s " +
           "WHERE s.userId = :userId")
    Optional<Short> getUserPlanType(@Param("userId") UUID userId);

    /**
     * Check if user can create a new conversation
     * Logic:
     * - FREE: max 2 active conversations
     * - PREMIUM: max 10 active conversations
     * - PRO: unlimited
     *
     * @return true if user can create new conversation, false otherwise
     */
    @Query("SELECT CASE WHEN " +
           "s.planId = 3 THEN true " +  // PRO (planId 3) can always create
           "WHEN s.planId = 2 THEN " +
           "  (SELECT COUNT(c) FROM ChatConversation c " +
           "   WHERE c.userId = :userId AND c.deletedAt IS NULL) < 10 " +  // PREMIUM (planId 2): < 10
           "WHEN s.planId = 1 THEN " +
           "  (SELECT COUNT(c) FROM ChatConversation c " +
           "   WHERE c.userId = :userId AND c.deletedAt IS NULL) < 2 " +   // FREE (planId 1): < 2
           "ELSE false " +
           "END " +
           "FROM ChatSession s " +
           "WHERE s.userId = :userId")
    boolean canCreateNewConversation(@Param("userId") UUID userId);

    /**
     * Get current active conversation count for a user
     * Used in quota enforcement logic
     */
    @Query("SELECT COUNT(c) FROM ChatConversation c " +
           "WHERE c.userId = :userId " +
           "AND c.deletedAt IS NULL")
    Long getActiveConversationCount(@Param("userId") UUID userId);

    /**
     * Get conversation limit for user's plan
     * Returns: 2 (FREE), 10 (PREMIUM), unlimited (PRO)
     */
    @Query("SELECT CASE " +
           "WHEN s.planId = 3 THEN 999 " +  // Unlimited (PRO), use high number
           "WHEN s.planId = 2 THEN 10 " +  // PREMIUM
           "WHEN s.planId = 1 THEN 2 " +  // FREE
           "ELSE 0 " +
           "END " +
           "FROM ChatSession s " +
           "WHERE s.userId = :userId")
    Long getConversationLimit(@Param("userId") UUID userId);

    /**
     * Upgrade user to a new plan
     * Updates plan type and reset dates as needed
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.planId = :newPlanId, s.planStartDate = :startDate, s.planRenewalDate = :renewalDate, s.updatedAt = :updatedAt " +
           "WHERE s.userId = :userId")
    void upgradePlan(
        @Param("userId") UUID userId,
        @Param("newPlanId") Short newPlanId,
        @Param("startDate") LocalDateTime startDate,
        @Param("renewalDate") LocalDateTime renewalDate,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Downgrade user to a lower plan
     * May require archiving excess conversations
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.planId = :newPlanId, s.planStartDate = :startDate, s.planRenewalDate = :renewalDate, s.updatedAt = :updatedAt " +
           "WHERE s.userId = :userId")
    void downgradePlan(
        @Param("userId") UUID userId,
        @Param("newPlanId") Short newPlanId,
        @Param("startDate") LocalDateTime startDate,
        @Param("renewalDate") LocalDateTime renewalDate,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Increment total message count for user
     * Used for analytics and usage tracking
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.totalMessagesSent = s.totalMessagesSent + 1 " +
           "WHERE s.userId = :userId")
    void incrementMessageCount(@Param("userId") UUID userId);

    /**
     * Bulk increment message count
     * Used when processing multiple messages
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.totalMessagesSent = s.totalMessagesSent + :count " +
           "WHERE s.userId = :userId")
    void incrementMessageCountBy(
        @Param("userId") UUID userId,
        @Param("count") Long count
    );

    /**
     * Sum total tokens used by user (for billing)
     * Across all conversations
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m " +
           "WHERE m.userId = :userId")
    Long getTotalTokensUsed(@Param("userId") UUID userId);

    /**
     * Get tokens used this month
     * For monthly billing if implemented
     */
    @Query("SELECT COALESCE(SUM(m.tokenCount), 0) FROM ChatMessage m " +
           "WHERE m.userId = :userId " +
           "AND m.createdAt >= :monthStart " +
           "AND m.createdAt < :monthEnd")
    Long getTokensUsedThisMonth(
        @Param("userId") UUID userId,
        @Param("monthStart") LocalDateTime monthStart,
        @Param("monthEnd") LocalDateTime monthEnd
    );

    /**
     * Update last activity timestamp
     * Track when user was last active
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.lastActivityAt = :lastActivity, s.updatedAt = :updatedAt " +
           "WHERE s.userId = :userId")
    void updateLastActivity(
        @Param("userId") UUID userId,
        @Param("lastActivity") LocalDateTime lastActivity,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Record conversation creation
     * Increments active conversation count
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.totalConversationsCreated = s.totalConversationsCreated + 1, " +
           "s.updatedAt = :updatedAt " +
           "WHERE s.userId = :userId")
    void recordConversationCreation(
        @Param("userId") UUID userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Check if plan renewal is needed
     * When planRenewalDate has passed, plan resets
     */
    @Query("SELECT CASE WHEN s.planRenewalDate < :currentTime THEN true ELSE false END " +
           "FROM ChatSession s " +
           "WHERE s.userId = :userId")
    boolean needsPlanRenewal(
        @Param("userId") UUID userId,
        @Param("currentTime") LocalDateTime currentTime
    );

    /**
     * Reset plan renewal
     * Called when subscription renews (monthly)
     */
    @Modifying
    @Transactional
    @Query("UPDATE ChatSession s " +
           "SET s.planStartDate = :newStartDate, " +
           "s.planRenewalDate = :newRenewalDate, " +
           "s.updatedAt = :updatedAt " +
           "WHERE s.userId = :userId")
    void resetPlanRenewal(
        @Param("userId") UUID userId,
        @Param("newStartDate") LocalDateTime newStartDate,
        @Param("newRenewalDate") LocalDateTime newRenewalDate,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Get sessions for users on FREE plan
     * Used for targeting upgrade prompts
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.planId = 1 " +
           "ORDER BY s.lastActivityAt DESC")
    java.util.List<ChatSession> getFreeUserSessions();

    /**
     * Get sessions for users on PREMIUM plan
     * Used for engagement/retention tracking
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.planId = 2 " +
           "ORDER BY s.planRenewalDate ASC")
    java.util.List<ChatSession> getPremiumUserSessions();

    /**
     * Get sessions for users on PRO plan
     * For VIP support or analytics
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.planId = 3 " +
           "ORDER BY s.planStartDate DESC")
    java.util.List<ChatSession> getProUserSessions();

    /**
     * Get users approaching plan renewal
     * Used for renewal reminders
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.planRenewalDate > :now " +
           "AND s.planRenewalDate < :soon " +
           "ORDER BY s.planRenewalDate ASC")
    java.util.List<ChatSession> getUsersApproachingRenewal(
        @Param("now") LocalDateTime now,
        @Param("soon") LocalDateTime soon
    );

    /**
     * Get active users (accessed in last N days)
     * For engagement analytics
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.lastActivityAt > :since " +
           "ORDER BY s.lastActivityAt DESC")
    java.util.List<ChatSession> getActiveUsers(@Param("since") LocalDateTime since);

    /**
     * Get inactive users (no activity for N days)
     * For re-engagement campaigns
     */
    @Query("SELECT s FROM ChatSession s " +
           "WHERE s.lastActivityAt < :beforeTime " +
           "ORDER BY s.lastActivityAt ASC")
    java.util.List<ChatSession> getInactiveUsers(@Param("beforeTime") LocalDateTime beforeTime);
}
