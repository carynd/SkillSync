package com.skillsync.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillsync.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ChatMessageDTO
 * Transfer object for chat messages in API responses
 *
 * Design Pattern: DTO separates entity structure from API contract
 * Benefits:
 * - API can evolve independently from database schema
 * - Hide sensitive fields
 * - Control what gets serialized to JSON
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDTO {

    /**
     * Unique message ID
     */
    @JsonProperty("message_id")
    private UUID messageId;

    /**
     * Who sent this message (user or assistant)
     */
    @JsonProperty("role")
    private String role;  // Enum name as string for JSON

    /**
     * The message content (question or response)
     */
    @JsonProperty("content")
    private String content;

    /**
     * When was this message created
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * Token count (only for cost estimation)
     * Frontend doesn't need this, but useful for monitoring
     */
    @JsonProperty("token_count")
    private Integer tokenCount;

    /**
     * Was this response from cache? (Only relevant for assistant messages)
     */
    @JsonProperty("is_cached")
    private Boolean isCached;

    /**
     * Create DTO from entity (conversion helper)
     */
    public static ChatMessageDTO fromEntity(com.skillsync.entity.ChatMessage entity) {
        return ChatMessageDTO.builder()
                .messageId(entity.getMessageId())
                .role(entity.getRole().name())
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .tokenCount(entity.getTokenCount())
                .isCached(entity.getIsCached())
                .build();
    }

    /**
     * Convert DTO to entity
     */
    public com.skillsync.entity.ChatMessage toEntity(UUID conversationId, UUID userId) {
        return com.skillsync.entity.ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(MessageRole.fromString(this.role))
                .content(this.content)
                .isCached(this.isCached != null && this.isCached)
                .build();
    }
}
