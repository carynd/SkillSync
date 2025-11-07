package com.skillsync.entity;

import com.skillsync.enums.AITone;
import com.skillsync.enums.ContextLevel;
import com.skillsync.enums.ResponseStyle;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * UserAIPreferences Entity
 * Stores how each user wants AI to respond
 *
 * Design Patterns:
 * - One-to-One relationship with User
 * - Uses enums for all selection fields (no free-text strings)
 * - Feature toggles for granular control
 * - Tracking of last used for engagement analytics
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Entity
@Table(
    name = "user_ai_preferences",
    indexes = {
        @Index(name = "idx_user_ai_preferences_user_id", columnList = "user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAIPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "preference_id", columnDefinition = "UUID")
    private UUID preferenceId;

    /**
     * User this preference belongs to (one-to-one)
     */
    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID userId;

    // ==================== CONTEXT & PERSONALIZATION ====================

    /**
     * How deeply should AI reference user's profile?
     * MINIMAL: No user data references
     * BALANCED: Mention goals/skills when relevant
     * DEEP: Actively reference profile, proactively suggest
     */
    @Column(name = "context_level_id", nullable = false)
    private Short contextLevelId;

    @Transient
    private ContextLevel contextLevel;

    // ==================== RESPONSE STYLE ====================

    /**
     * How verbose should responses be?
     * CONCISE: 2-3 paragraphs, ~500 tokens
     * BALANCED: 5-7 paragraphs, ~1000 tokens (default)
     * DETAILED: All angles covered, ~1500 tokens
     */
    @Column(name = "response_style_id", nullable = false)
    private Short responseStyleId;

    @Transient
    private ResponseStyle responseStyle;

    /**
     * What personality should AI have?
     * PROFESSIONAL: Formal, direct
     * CASUAL: Friendly, conversational
     * MENTORING: Encouraging, supportive (default)
     */
    @Column(name = "tone_id", nullable = false)
    private Short toneId;

    @Transient
    private AITone tone;

    // ==================== FEATURE TOGGLES ====================

    /**
     * Include learning resources and links when appropriate
     */
    @Column(name = "include_resources", nullable = false)
    @Builder.Default
    private Boolean includeResources = true;

    /**
     * Include code examples and practical demonstrations
     */
    @Column(name = "include_examples", nullable = false)
    @Builder.Default
    private Boolean includeExamples = true;

    /**
     * Include time estimates and learning timeline
     */
    @Column(name = "include_timeline", nullable = false)
    @Builder.Default
    private Boolean includeTimeline = true;

    /**
     * Use user's profile information in responses
     */
    @Column(name = "include_user_context", nullable = false)
    @Builder.Default
    private Boolean includeUserContext = true;

    // ==================== UI PREFERENCES ====================

    /**
     * Show customize button in UI
     */
    @Column(name = "show_customize_button", nullable = false)
    @Builder.Default
    private Boolean showCustomizeButton = true;

    /**
     * Show tips/hints on first messages
     */
    @Column(name = "show_tips_on_first_messages", nullable = false)
    @Builder.Default
    private Boolean showTipsOnFirstMessages = true;

    /**
     * Automatically summarize conversations
     */
    @Column(name = "auto_summarize_conversations", nullable = false)
    @Builder.Default
    private Boolean autoSummarizeConversations = true;

    // ==================== AUDIT FIELDS ====================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * When were these preferences last actually used in an AI call
     * Used for engagement tracking
     */
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Load enums from IDs after entity is loaded from DB
     */
    @PostLoad
    public void loadEnums() {
        if (this.contextLevelId != null) {
            this.contextLevel = ContextLevel.fromDatabaseId(this.contextLevelId);
        }
        if (this.responseStyleId != null) {
            this.responseStyle = ResponseStyle.fromDatabaseId(this.responseStyleId);
        }
        if (this.toneId != null) {
            this.tone = AITone.fromDatabaseId(this.toneId);
        }
    }

    /**
     * Save enums to IDs before entity is saved to DB
     */
    @PrePersist
    @PreUpdate
    public void saveEnums() {
        if (this.contextLevel != null) {
            this.contextLevelId = this.contextLevel.getDatabaseId();
        }
        if (this.responseStyle != null) {
            this.responseStyleId = this.responseStyle.getDatabaseId();
        }
        if (this.tone != null) {
            this.toneId = this.tone.getDatabaseId();
        }
    }

    /**
     * Reset to default preferences
     * Called when user clicks "Reset to Default"
     */
    public void resetToDefaults() {
        this.contextLevel = ContextLevel.getDefault();
        this.responseStyle = ResponseStyle.getDefault();
        this.tone = AITone.getDefault();
        this.includeResources = true;
        this.includeExamples = true;
        this.includeTimeline = true;
        this.includeUserContext = true;
        this.showCustomizeButton = true;
        this.showTipsOnFirstMessages = true;
        this.autoSummarizeConversations = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Apply a preset preference profile
     */
    public void applyPreset(String presetName) {
        switch (presetName.toLowerCase()) {
            case "study_mode":
                // Study mode: include everything, detailed responses, mentoring tone
                this.contextLevel = ContextLevel.BALANCED;
                this.responseStyle = ResponseStyle.DETAILED;
                this.tone = AITone.MENTORING;
                this.includeResources = true;
                this.includeExamples = true;
                this.includeTimeline = true;
                break;

            case "interview_prep":
                // Interview prep: concise, professional tone, no hand-holding
                this.contextLevel = ContextLevel.MINIMAL;
                this.responseStyle = ResponseStyle.CONCISE;
                this.tone = AITone.PROFESSIONAL;
                this.includeResources = true;
                this.includeExamples = false;
                this.includeTimeline = false;
                break;

            case "career_mentor":
                // Career mentor: deep context, detailed, mentoring tone
                this.contextLevel = ContextLevel.DEEP;
                this.responseStyle = ResponseStyle.DETAILED;
                this.tone = AITone.MENTORING;
                this.includeResources = true;
                this.includeExamples = true;
                this.includeTimeline = true;
                break;

            default:
                throw new IllegalArgumentException("Unknown preset: " + presetName);
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Record that these preferences were used
     */
    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
    }

    /**
     * Get max tokens limit based on response style
     * Used to configure Gemini API
     */
    public int getMaxTokensForResponse() {
        if (this.responseStyle != null) {
            return this.responseStyle.getMaxTokens();
        }
        return ResponseStyle.BALANCED.getMaxTokens();
    }

    @Override
    public String toString() {
        return "UserAIPreferences{" +
                "userId=" + userId +
                ", contextLevel=" + contextLevel +
                ", responseStyle=" + responseStyle +
                ", tone=" + tone +
                ", includeResources=" + includeResources +
                ", includeExamples=" + includeExamples +
                ", includeTimeline=" + includeTimeline +
                '}';
    }
}
