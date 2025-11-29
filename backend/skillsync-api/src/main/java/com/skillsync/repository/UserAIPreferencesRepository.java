package com.skillsync.repository;

import com.skillsync.entity.UserAIPreferences;
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
 * UserAIPreferencesRepository
 * Spring Data JPA repository for UserAIPreferences entity
 *
 * Provides:
 * - One-to-one relationship with User entity
 * - CRUD operations for user AI customization preferences
 * - Bulk operations for preference updates
 * - Usage tracking for analytics
 *
 * Design Pattern: Repository Pattern + One-to-One Relationship
 *
 * Purpose:
 * - Store user customization preferences (context level, response style, tone)
 * - Track feature toggles (include resources, examples, timeline, etc)
 * - Manage preset configurations (study mode, interview prep, career mentor)
 * - Record usage for analytics
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Repository
public interface UserAIPreferencesRepository extends JpaRepository<UserAIPreferences, UUID> {

    /**
     * Find preferences for a specific user
     * One-to-one relationship: Each user has exactly one preferences record
     *
     * @param userId The user's ID
     * @return Optional containing preferences if user exists
     */
    Optional<UserAIPreferences> findByUserId(UUID userId);

    /**
     * Check if preferences exist for a user
     * @return true if user has preferences, false otherwise
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM UserAIPreferences p " +
           "WHERE p.userId = :userId")
    boolean existsForUser(@Param("userId") UUID userId);

    /**
     * Update context level for a user
     * Controls how much user profile is referenced in AI responses
     * Values: MINIMAL (1), BALANCED (2), DEEP (3)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = :contextLevelId, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateContextLevel(
        @Param("userId") UUID userId,
        @Param("contextLevelId") Short contextLevelId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Update response style for a user
     * Controls verbosity and token limits
     * Values: CONCISE (1), BALANCED (2), DETAILED (3)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.responseStyleId = :responseStyleId, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateResponseStyle(
        @Param("userId") UUID userId,
        @Param("responseStyleId") Short responseStyleId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Update AI tone for a user
     * Controls personality and response characteristics
     * Values: PROFESSIONAL (1), CASUAL (2), MENTORING (3)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.toneId = :toneId, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateTone(
        @Param("userId") UUID userId,
        @Param("toneId") Short toneId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle inclusion of resources in responses
     * Resources: Tutorials, courses, documentation links
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.includeResources = :includeResources, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateIncludeResources(
        @Param("userId") UUID userId,
        @Param("includeResources") Boolean includeResources,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle inclusion of code examples in responses
     * Examples: Code snippets, implementation patterns
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.includeExamples = :includeExamples, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateIncludeExamples(
        @Param("userId") UUID userId,
        @Param("includeExamples") Boolean includeExamples,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle inclusion of timeline/time estimates
     * Timeline: How long tasks will take, milestone estimates
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.includeTimeline = :includeTimeline, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateIncludeTimeline(
        @Param("userId") UUID userId,
        @Param("includeTimeline") Boolean includeTimeline,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle inclusion of user context in responses
     * User Context: Reference to their profile, goals, skills
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.includeUserContext = :includeUserContext, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateIncludeUserContext(
        @Param("userId") UUID userId,
        @Param("includeUserContext") Boolean includeUserContext,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle customize button visibility
     * Shows/hides UI button to access preferences panel
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.showCustomizeButton = :showButton, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateShowCustomizeButton(
        @Param("userId") UUID userId,
        @Param("showButton") Boolean showButton,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle tips on first messages
     * Shows educational tips for first-time users
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.showTipsOnFirstMessages = :showTips, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateShowTipsOnFirstMessages(
        @Param("userId") UUID userId,
        @Param("showTips") Boolean showTips,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Toggle auto-summarization of conversations
     * Automatically summarizes conversations every 20 messages
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.autoSummarizeConversations = :autoSummarize, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateAutoSummarizeConversations(
        @Param("userId") UUID userId,
        @Param("autoSummarize") Boolean autoSummarize,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Record usage of preferences (last accessed)
     * Used for analytics: which users are accessing preferences
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.lastUsedAt = :lastUsedAt, p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void recordUsage(
        @Param("userId") UUID userId,
        @Param("lastUsedAt") LocalDateTime lastUsedAt,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Reset preferences to default values
     * Resets all preferences to their default state:
     * - contextLevel: BALANCED
     * - responseStyle: BALANCED
     * - tone: PROFESSIONAL
     * - All feature toggles: true (enabled)
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = 2, " +  // BALANCED
           "p.responseStyleId = 2, " +     // BALANCED
           "p.toneId = 1, " +              // PROFESSIONAL
           "p.includeResources = true, " +
           "p.includeExamples = true, " +
           "p.includeTimeline = true, " +
           "p.includeUserContext = true, " +
           "p.showCustomizeButton = true, " +
           "p.showTipsOnFirstMessages = true, " +
           "p.autoSummarizeConversations = true, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void resetToDefaults(
        @Param("userId") UUID userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Apply "Study Mode" preset
     * - Context: DEEP (references user goals and skills)
     * - Style: DETAILED (comprehensive explanations)
     * - Tone: MENTORING (encouraging and supportive)
     * - All features enabled for learning
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = 3, " +  // DEEP
           "p.responseStyleId = 3, " +     // DETAILED
           "p.toneId = 3, " +              // MENTORING
           "p.includeResources = true, " +
           "p.includeExamples = true, " +
           "p.includeTimeline = true, " +
           "p.includeUserContext = true, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void applyStudyModePreset(
        @Param("userId") UUID userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Apply "Interview Prep" preset
     * - Context: BALANCED (some profile reference)
     * - Style: CONCISE (quick, focused responses)
     * - Tone: PROFESSIONAL (formal, direct)
     * - Enable resources and examples, disable timeline
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = 2, " +  // BALANCED
           "p.responseStyleId = 1, " +     // CONCISE
           "p.toneId = 1, " +              // PROFESSIONAL
           "p.includeResources = true, " +
           "p.includeExamples = true, " +
           "p.includeTimeline = false, " +
           "p.includeUserContext = true, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void applyInterviewPrepPreset(
        @Param("userId") UUID userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Apply "Career Mentor" preset
     * - Context: DEEP (full profile reference)
     * - Style: BALANCED (comprehensive but concise)
     * - Tone: MENTORING (encouraging, supportive)
     * - All features enabled for personalized guidance
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = 3, " +  // DEEP
           "p.responseStyleId = 2, " +     // BALANCED
           "p.toneId = 3, " +              // MENTORING
           "p.includeResources = true, " +
           "p.includeExamples = true, " +
           "p.includeTimeline = true, " +
           "p.includeUserContext = true, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void applyCareerMentorPreset(
        @Param("userId") UUID userId,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Bulk update all preferences at once
     * Used when user saves the entire preferences form
     */
    @Modifying
    @Transactional
    @Query("UPDATE UserAIPreferences p " +
           "SET p.contextLevelId = :contextLevelId, " +
           "p.responseStyleId = :responseStyleId, " +
           "p.toneId = :toneId, " +
           "p.includeResources = :includeResources, " +
           "p.includeExamples = :includeExamples, " +
           "p.includeTimeline = :includeTimeline, " +
           "p.includeUserContext = :includeUserContext, " +
           "p.showCustomizeButton = :showCustomizeButton, " +
           "p.showTipsOnFirstMessages = :showTipsOnFirstMessages, " +
           "p.autoSummarizeConversations = :autoSummarizeConversations, " +
           "p.updatedAt = :updatedAt " +
           "WHERE p.userId = :userId")
    void updateAllPreferences(
        @Param("userId") UUID userId,
        @Param("contextLevelId") Short contextLevelId,
        @Param("responseStyleId") Short responseStyleId,
        @Param("toneId") Short toneId,
        @Param("includeResources") Boolean includeResources,
        @Param("includeExamples") Boolean includeExamples,
        @Param("includeTimeline") Boolean includeTimeline,
        @Param("includeUserContext") Boolean includeUserContext,
        @Param("showCustomizeButton") Boolean showCustomizeButton,
        @Param("showTipsOnFirstMessages") Boolean showTipsOnFirstMessages,
        @Param("autoSummarizeConversations") Boolean autoSummarizeConversations,
        @Param("updatedAt") LocalDateTime updatedAt
    );

    /**
     * Check if user has customized preferences
     * Returns true if any preference differs from defaults
     * Used to show "Reset to Defaults" button in UI
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
           "FROM UserAIPreferences p " +
           "WHERE p.userId = :userId " +
           "AND (p.contextLevelId != 2 " +  // Not BALANCED
           "OR p.responseStyleId != 2 " +   // Not BALANCED
           "OR p.toneId != 1 " +            // Not PROFESSIONAL
           "OR p.includeResources = false " +
           "OR p.includeExamples = false " +
           "OR p.includeTimeline = false " +
           "OR p.includeUserContext = false)")
    boolean hasCustomizedPreferences(@Param("userId") UUID userId);

    /**
     * Get users who haven't accessed preferences yet
     * Used for engagement/onboarding tracking
     */
    @Query("SELECT p FROM UserAIPreferences p " +
           "WHERE p.lastUsedAt IS NULL " +
           "AND p.createdAt < :thresholdTime")
    java.util.List<UserAIPreferences> findUsersNeverAccessedPreferences(
        @Param("thresholdTime") LocalDateTime thresholdTime
    );
}
