package com.skillsync.entity;

import com.skillsync.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessage Entity
 * Represents a single message in a conversation
 *
 * Strategy: Only stores recent messages (hot data)
 * Older messages are archived to MongoDB
 *
 * Design Patterns:
 * - Soft delete for audit trails
 * - Token counting for cost estimation
 * - Cache flag for fallback detection
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Entity
@Table(
    name = "chat_messages",
    indexes = {
        @Index(name = "idx_chat_messages_conversation_created", columnList = "conversation_id, created_at DESC"),
        @Index(name = "idx_chat_messages_user_created", columnList = "user_id, created_at DESC"),
        @Index(name = "idx_chat_messages_role", columnList = "role_id"),
        @Index(name = "idx_chat_messages_cached", columnList = "conversation_id, is_cached")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "message_id", columnDefinition = "UUID")
    private UUID messageId;

    @Column(name = "conversation_id", nullable = false, columnDefinition = "UUID")
    private UUID conversationId;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    /**
     * Who sent this message (USER, ASSISTANT, SYSTEM)
     * Foreign key to message_role table
     */
    @Column(name = "role_id", nullable = false)
    private Short roleId;

    /**
     * Transient field - not stored in DB, loaded from roleId
     */
    @Transient
    private MessageRole role;

    /**
     * The actual message content
     * Can be user question or AI response
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * Token count of this message
     * Used for:
     * - API cost estimation
     * - Rate limiting
     * - Context window management
     */
    @Column(name = "token_count")
    private Integer tokenCount;

    /**
     * Flag: Is this response from cache?
     * When Gemini API is down, we serve cached responses
     * This flag indicates that this message was served from cache
     */
    @Column(name = "is_cached")
    @Builder.Default
    private Boolean isCached = false;

    /**
     * Gemini finish reason (if this is AI response)
     * Possible values: STOP, SAFETY, MAX_TOKENS, etc.
     * Used for debugging and error analysis
     */
    @Column(name = "gemini_finish_reason", length = 50)
    private String geminiFinishReason;

    /**
     * Audit: When was this message created
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Soft delete: When was this message deleted (NULL = not deleted)
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Load enum from roleId after entity is loaded from DB
     */
    @PostLoad
    public void loadEnum() {
        if (this.roleId != null) {
            this.role = MessageRole.fromDatabaseId(this.roleId);
        }
    }

    /**
     * Save enum to roleId before entity is saved to DB
     */
    @PrePersist
    @PreUpdate
    public void saveEnum() {
        if (this.role != null) {
            this.roleId = this.role.getDatabaseId();
        }
    }

    /**
     * Check if this is a user message
     */
    public boolean isUserMessage() {
        return this.role == MessageRole.USER;
    }

    /**
     * Check if this is an AI response
     */
    public boolean isAssistantMessage() {
        return this.role == MessageRole.ASSISTANT;
    }

    /**
     * Check if this is a system message
     */
    public boolean isSystemMessage() {
        return this.role == MessageRole.SYSTEM;
    }

    /**
     * Check if message has been soft-deleted
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    /**
     * Soft delete this message
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * Get approximate word count from token count
     * 1 token ≈ 0.75 words (rough estimate)
     */
    public int getApproximateWordCount() {
        if (tokenCount == null) {
            return 0;
        }
        return (int) (tokenCount * 0.75);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "messageId=" + messageId +
                ", conversationId=" + conversationId +
                ", role=" + role +
                ", tokenCount=" + tokenCount +
                ", isCached=" + isCached +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
}
