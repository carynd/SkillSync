package com.skillsync.entity;

import com.skillsync.enums.PlanType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatSession Entity
 * Tracks user subscription plan and conversation quota
 *
 * Design Patterns:
 * - One-to-one with User entity
 * - Denormalized fields for performance (max_conversations_allowed, max_messages_per_month)
 * - Enum bidirectional mapping for plan type
 * - Audit fields for compliance
 *
 * Purpose:
 * Enforces monetization model:
 * - FREE: 2 concurrent conversations
 * - PREMIUM: 10 concurrent conversations
 * - PRO: Unlimited conversations
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Entity
@Table(
    name = "chat_sessions",
    indexes = {
        @Index(name = "idx_chat_sessions_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_sessions_plan_id", columnList = "plan_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id", columnDefinition = "UUID")
    private UUID sessionId;

    /**
     * User ID - One-to-one relationship with User
     * UNIQUE constraint: Each user has exactly one session
     */
    @Column(name = "user_id", nullable = false, unique = true, columnDefinition = "UUID")
    private UUID userId;

    /**
     * User's current subscription plan (FREE, PREMIUM, PRO)
     * Stored as foreign key to plan_type table
     */
    @Column(name = "plan_id", nullable = false)
    private Short planId;

    /**
     * Transient field - not stored in DB, loaded from planId
     * Used in application code for type-safety
     */
    @Transient
    private PlanType planType;

    /**
     * Count of active (non-deleted) conversations for this user
     * Denormalized for quick quota checks
     * Updated each time conversation is created/deleted
     */
    @Column(name = "active_conversations_count")
    @Builder.Default
    private Integer activeConversationsCount = 0;

    /**
     * Maximum conversations allowed based on plan
     * Denormalized from plan_type table for performance
     * Values: 2 (FREE), 10 (PREMIUM), 999 (PRO/unlimited)
     */
    @Column(name = "max_conversations_allowed", nullable = false)
    @Builder.Default
    private Integer maxConversationsAllowed = 2;

    /**
     * Messages used in current billing period
     * Reset when plan renews
     */
    @Column(name = "messages_used_this_month")
    @Builder.Default
    private Integer messagesUsedThisMonth = 0;

    /**
     * Maximum messages allowed per month (if applicable)
     * Denormalized from plan_type table
     * Values: 100 (FREE), 1000 (PREMIUM), 999999 (PRO/unlimited)
     */
    @Column(name = "max_messages_per_month", nullable = false)
    @Builder.Default
    private Integer maxMessagesPerMonth = 100;

    /**
     * When current plan period started
     * Used to calculate when plan renews
     */
    @Column(name = "plan_started_at", nullable = false)
    @Builder.Default
    private LocalDateTime planStartDate = LocalDateTime.now();

    /**
     * When current plan renews (monthly/yearly depending on plan)
     * After this date, subscription needs renewal
     */
    @Column(name = "plan_renews_at")
    private LocalDateTime planRenewalDate;

    /**
     * Audit: When was this session created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit: When was this session last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Load plan type enum from planId after entity is loaded from DB
     */
    @PostLoad
    public void loadEnum() {
        if (this.planId != null) {
            this.planType = PlanType.fromDatabaseId(this.planId);
        }
    }

    /**
     * Save plan type enum to planId before entity is saved to DB
     */
    @PrePersist
    @PreUpdate
    public void saveEnum() {
        if (this.planType != null) {
            this.planId = this.planType.getDatabaseId();
        }
    }

    /**
     * Check if user can create another conversation
     * Respects plan limits:
     * - FREE: max 2
     * - PREMIUM: max 10
     * - PRO: unlimited
     */
    public boolean canCreateConversation() {
        return this.activeConversationsCount < this.maxConversationsAllowed;
    }

    /**
     * Check if user can send a message
     * Respects monthly message limits (if applicable)
     */
    public boolean canSendMessage() {
        return this.messagesUsedThisMonth < this.maxMessagesPerMonth;
    }

    /**
     * Check how many conversations the user has quota for
     * Negative value = over quota
     */
    public int getConversationQuotaRemaining() {
        return Math.max(0, this.maxConversationsAllowed - this.activeConversationsCount);
    }

    /**
     * Get percentage of monthly message quota used
     */
    public double getMessageQuotaPercentage() {
        if (this.maxMessagesPerMonth == 0 || this.maxMessagesPerMonth >= 999999) {
            return 0.0;  // Unlimited
        }
        return (double) this.messagesUsedThisMonth / this.maxMessagesPerMonth * 100.0;
    }

    /**
     * Check if plan renewal is overdue
     */
    public boolean needsRenewal() {
        if (this.planRenewalDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.planRenewalDate);
    }

    /**
     * Check if plan renewal is approaching (within 7 days)
     */
    public boolean renewalApproaching() {
        if (this.planRenewalDate == null) {
            return false;
        }
        LocalDateTime soonTime = LocalDateTime.now().plusDays(7);
        return this.planRenewalDate.isBefore(soonTime) && this.planRenewalDate.isAfter(LocalDateTime.now());
    }

    /**
     * Get display name of current plan
     */
    public String getPlanName() {
        if (this.planType == null) {
            return "UNKNOWN";
        }
        return this.planType.name();
    }

    /**
     * Additional tracking: Total messages sent (not just this month)
     * Used for analytics dashboard
     */
    @Column(name = "total_messages_sent")
    @Builder.Default
    private Long totalMessagesSent = 0L;

    /**
     * Track total conversations ever created
     * Used for user engagement metrics
     */
    @Column(name = "total_conversations_created")
    @Builder.Default
    private Long totalConversationsCreated = 0L;

    /**
     * Last time user was active
     * Used for inactive user detection and re-engagement campaigns
     */
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Override
    public String toString() {
        return "ChatSession{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", planType=" + planType +
                ", activeConversationsCount=" + activeConversationsCount +
                ", maxConversationsAllowed=" + maxConversationsAllowed +
                ", messagesUsedThisMonth=" + messagesUsedThisMonth +
                ", planRenewalDate=" + planRenewalDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
