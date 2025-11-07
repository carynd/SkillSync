package com.skillsync.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ChatConversationDTO
 * Transfer object for conversations in API responses
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatConversationDTO {

    /**
     * Unique conversation ID
     */
    @JsonProperty("conversation_id")
    private UUID conversationId;

    /**
     * Conversation title (for display in sidebar)
     */
    @JsonProperty("title")
    private String title;

    /**
     * Conversation description (optional)
     */
    @JsonProperty("description")
    private String description;

    /**
     * Current status (ACTIVE, ARCHIVED, DELETED, PAUSED)
     */
    @JsonProperty("status")
    private String status;

    /**
     * Number of messages in this conversation
     */
    @JsonProperty("message_count")
    private Integer messageCount;

    /**
     * When was the last message sent
     */
    @JsonProperty("last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * When was this conversation created
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * Recent messages (limited to last 10 for initial load)
     * If user needs more, pagination handles it
     */
    @JsonProperty("messages")
    private List<ChatMessageDTO> messages;

    /**
     * Create DTO from entity
     */
    public static ChatConversationDTO fromEntity(com.skillsync.entity.ChatConversation entity) {
        return ChatConversationDTO.builder()
                .conversationId(entity.getConversationId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .status(entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN")
                .messageCount(entity.getMessageCount())
                .lastMessageAt(entity.getLastMessageAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Create DTO from entity with full message history
     */
    public static ChatConversationDTO fromEntityWithMessages(
            com.skillsync.entity.ChatConversation entity,
            List<ChatMessageDTO> messages) {
        ChatConversationDTO dto = fromEntity(entity);
        dto.setMessages(messages);
        return dto;
    }
}
