package com.skillsync.dto.preferences;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillsync.enums.AITone;
import com.skillsync.enums.ContextLevel;
import com.skillsync.enums.ResponseStyle;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UserAIPreferencesDTO
 * Transfer object for user AI preferences in API requests/responses
 *
 * Design Pattern: DTO with validation annotations
 * Ensures data consistency at API boundary
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAIPreferencesDTO {

    /**
     * How deeply should AI reference user's profile
     * Values: MINIMAL, BALANCED, DEEP
     */
    @JsonProperty("context_level")
    @NotNull(message = "Context level is required")
    private String contextLevel;

    /**
     * How verbose should responses be
     * Values: CONCISE, BALANCED, DETAILED
     */
    @JsonProperty("response_style")
    @NotNull(message = "Response style is required")
    private String responseStyle;

    /**
     * What personality should AI have
     * Values: PROFESSIONAL, CASUAL, MENTORING
     */
    @JsonProperty("tone")
    @NotNull(message = "Tone is required")
    private String tone;

    /**
     * Include learning resources and links
     */
    @JsonProperty("include_resources")
    private Boolean includeResources;

    /**
     * Include code examples
     */
    @JsonProperty("include_examples")
    private Boolean includeExamples;

    /**
     * Include time estimates
     */
    @JsonProperty("include_timeline")
    private Boolean includeTimeline;

    /**
     * Use user's profile information
     */
    @JsonProperty("include_user_context")
    private Boolean includeUserContext;

    /**
     * Show customize button in UI
     */
    @JsonProperty("show_customize_button")
    private Boolean showCustomizeButton;

    /**
     * Show tips on first messages
     */
    @JsonProperty("show_tips_on_first_messages")
    private Boolean showTipsOnFirstMessages;

    /**
     * Auto-summarize conversations
     */
    @JsonProperty("auto_summarize_conversations")
    private Boolean autoSummarizeConversations;

    /**
     * Create DTO from entity
     */
    public static UserAIPreferencesDTO fromEntity(com.skillsync.entity.UserAIPreferences entity) {
        return UserAIPreferencesDTO.builder()
                .contextLevel(entity.getContextLevel().name())
                .responseStyle(entity.getResponseStyle().name())
                .tone(entity.getTone().name())
                .includeResources(entity.getIncludeResources())
                .includeExamples(entity.getIncludeExamples())
                .includeTimeline(entity.getIncludeTimeline())
                .includeUserContext(entity.getIncludeUserContext())
                .showCustomizeButton(entity.getShowCustomizeButton())
                .showTipsOnFirstMessages(entity.getShowTipsOnFirstMessages())
                .autoSummarizeConversations(entity.getAutoSummarizeConversations())
                .build();
    }

    /**
     * Convert DTO to entity for persistence
     */
    public com.skillsync.entity.UserAIPreferences toEntity() {
        return com.skillsync.entity.UserAIPreferences.builder()
                .contextLevel(ContextLevel.fromString(this.contextLevel))
                .responseStyle(ResponseStyle.fromString(this.responseStyle))
                .tone(AITone.fromString(this.tone))
                .includeResources(this.includeResources != null ? this.includeResources : true)
                .includeExamples(this.includeExamples != null ? this.includeExamples : true)
                .includeTimeline(this.includeTimeline != null ? this.includeTimeline : true)
                .includeUserContext(this.includeUserContext != null ? this.includeUserContext : true)
                .showCustomizeButton(this.showCustomizeButton != null ? this.showCustomizeButton : true)
                .showTipsOnFirstMessages(this.showTipsOnFirstMessages != null ? this.showTipsOnFirstMessages : true)
                .autoSummarizeConversations(this.autoSummarizeConversations != null ? this.autoSummarizeConversations : true)
                .build();
    }

    /**
     * Merge DTO with existing entity (for PATCH requests)
     */
    public void mergeIntoEntity(com.skillsync.entity.UserAIPreferences entity) {
        if (this.contextLevel != null) {
            entity.setContextLevel(ContextLevel.fromString(this.contextLevel));
        }
        if (this.responseStyle != null) {
            entity.setResponseStyle(ResponseStyle.fromString(this.responseStyle));
        }
        if (this.tone != null) {
            entity.setTone(AITone.fromString(this.tone));
        }
        if (this.includeResources != null) {
            entity.setIncludeResources(this.includeResources);
        }
        if (this.includeExamples != null) {
            entity.setIncludeExamples(this.includeExamples);
        }
        if (this.includeTimeline != null) {
            entity.setIncludeTimeline(this.includeTimeline);
        }
        if (this.includeUserContext != null) {
            entity.setIncludeUserContext(this.includeUserContext);
        }
        if (this.showCustomizeButton != null) {
            entity.setShowCustomizeButton(this.showCustomizeButton);
        }
        if (this.showTipsOnFirstMessages != null) {
            entity.setShowTipsOnFirstMessages(this.showTipsOnFirstMessages);
        }
        if (this.autoSummarizeConversations != null) {
            entity.setAutoSummarizeConversations(this.autoSummarizeConversations);
        }
    }
}
