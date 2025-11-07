package com.skillsync.entity;

import com.skillsync.enums.ConversationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatConversation Entity
 * Represents a single conversation between user and AI
 *
 * Design Patterns:
 * - Lombok annotations for boilerplate reduction
 * - JPA annotations for ORM mapping
 * - Audit fields for compliance and debugging
 * - Soft delete support for data recovery
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Entity
@Table(
    name = "chat_conversations",
    indexes = {
        @Index(name = "idx_chat_conversations_user_id", columnList = "user_id"),
        @Index(name = "idx_chat_conversations_user_created", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_chat_conversations_status", columnList = "status_id"),
        @Index(name = "idx_chat_conversations_last_message", columnList = "last_message_at DESC")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "conversation_id", columnDefinition = "UUID")
    private UUID conversationId;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    /**
     * Conversation title (auto-generated from first few messages)
     * Used for UI display and user reference
     */
    @Column(name = "title", length = 255)
    private String title;

    /**
     * Conversation description (optional, user can set)
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Status of conversation (ACTIVE, ARCHIVED, DELETED, PAUSED)
     * Stored as foreign key to conversation_status table
     */
    @Column(name = "status_id", nullable = false)
    private Short statusId;

    /**
     * Transient field - not stored in DB, loaded from statusId
     * Used in application code for type-safety
     */
    @Transient
    private ConversationStatus status;

    /**
     * Message count (denormalized for performance)
     * Prevents counting messages each time we list conversations
     */
    @Column(name = "message_count")
    @Builder.Default
    private Integer messageCount = 0;

    /**
     * ID of the last message in this conversation
     * Used for quick lookups of latest message
     */
    @Column(name = "last_message_id", columnDefinition = "UUID")
    private UUID lastMessageId;

    /**
     * Timestamp of last message
     * Used for sorting and finding active conversations
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * AI-generated summary of conversation
     * Refreshed every 20 messages to prevent token bloat in Gemini context
     */
    @Column(name = "conversation_summary", columnDefinition = "TEXT")
    private String conversationSummary;

    /**
     * When the summary was last updated
     * Used to determine when to regenerate summary
     */
    @Column(name = "summary_last_updated")
    private LocalDateTime summaryLastUpdated;

    /**
     * Audit: When was this conversation created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Audit: When was this conversation last updated
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Soft delete: When was this conversation deleted (NULL = not deleted)
     * Allows recovery and audit trails
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Load enum from statusId after entity is loaded from DB
     */
    @PostLoad
    public void loadEnum() {
        if (this.statusId != null) {
            this.status = ConversationStatus.fromDatabaseId(this.statusId);
        }
    }

    /**
     * Save enum to statusId before entity is saved to DB
     */
    @PrePersist
    @PreUpdate
    public void saveEnum() {
        if (this.status != null) {
            this.statusId = this.status.getDatabaseId();
        }
    }

    /**
     * Check if conversation is active (not deleted, not archived)
     */
    public boolean isActive() {
        return this.deletedAt == null && this.status == ConversationStatus.ACTIVE;
    }

    /**
     * Soft delete this conversation
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.status = ConversationStatus.DELETED;
    }

    /**
     * Check if summary needs refresh
     * Summary should be refreshed every N messages or after N minutes
     */
    public boolean needsSummaryRefresh(int messagesSinceLastSummary, int minutesSinceLastSummary) {
        if (this.summaryLastUpdated == null) {
            return true;  // Never summarized
        }

        boolean tooManyMessagesSinceSummary = messagesSinceLastSummary >= 20;
        boolean tooMuchTimeSinceSummary = LocalDateTime.now()
            .minusMinutes(minutesSinceLastSummary)
            .isAfter(this.summaryLastUpdated);

        return tooManyMessagesSinceSummary || tooMuchTimeSinceSummary;
    }

    @Override
    public String toString() {
        return "ChatConversation{" +
                "conversationId=" + conversationId +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", messageCount=" + messageCount +
                ", lastMessageAt=" + lastMessageAt +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
