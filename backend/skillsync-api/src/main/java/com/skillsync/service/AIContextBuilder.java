package com.skillsync.service;

import com.skillsync.entity.ChatMessage;
import com.skillsync.entity.UserAIPreferences;
import com.skillsync.enums.ContextLevel;
import com.skillsync.enums.MessageRole;
import com.skillsync.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AIContextBuilder
 * Constructs rich context for AI prompts respecting user preferences
 *
 * Context building strategy:
 * - MINIMAL: Only conversation history, no user profile
 * - BALANCED: Recent conversation history + key user skills
 * - DEEP: Full conversation history + complete user profile + recommendations
 *
 * This builder ensures AI responses are personalized without overwhelming the model.
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIContextBuilder {

    /**
     * Build context for AI based on user preferences and conversation history
     *
     * @param user User entity with profile information
     * @param preferences User's AI preferences (context level, response style, etc.)
     * @param conversationHistory Messages in current conversation
     * @return Formatted context string for AI prompt
     */
    public String buildContext(User user, UserAIPreferences preferences, List<ChatMessage> conversationHistory) {
        log.debug("Building context with context level: {}",
                preferences != null ? preferences.getContextLevel() : "DEFAULT");

        StringBuilder context = new StringBuilder();

        // Always include response style and tone guidance
        context.append(buildInstructionsSection(preferences));

        // Include conversation history
        context.append(buildConversationHistorySection(conversationHistory));

        // Include user context based on preference level (only if user is provided)
        if (user != null) {
            context.append(buildUserContextSection(user, preferences));
        }

        return context.toString();
    }

    /**
     * Build instructions for AI on how to respond
     */
    private String buildInstructionsSection(UserAIPreferences preferences) {
        StringBuilder sb = new StringBuilder();

        sb.append("## Response Guidelines\n\n");

        // Use defaults if preferences are null
        if (preferences == null) {
            sb.append("- Provide helpful, balanced career and skill development advice.\n");
            sb.append("- Be professional and encouraging in your responses.\n");
            sb.append("\n");
            return sb.toString();
        }

        // Response Style
        switch (preferences.getResponseStyle()) {
            case CONCISE:
                sb.append("- Be concise and direct. Keep responses short and to the point.\n");
                break;
            case BALANCED:
                sb.append("- Provide balanced responses with necessary context and details.\n");
                break;
            case DETAILED:
                sb.append("- Provide detailed, comprehensive responses with thorough explanations.\n");
                break;
        }

        // Tone
        switch (preferences.getTone()) {
            case PROFESSIONAL:
                sb.append("- Use professional and formal language.\n");
                break;
            case CASUAL:
                sb.append("- Use friendly and conversational language.\n");
                break;
            case MENTORING:
                sb.append("- Act as a mentor, guide the user to discover answers, be encouraging.\n");
                break;
        }

        // Content inclusion preferences
        if (preferences.getIncludeResources()) {
            sb.append("- Include relevant learning resources and links when applicable.\n");
        }
        if (preferences.getIncludeExamples()) {
            sb.append("- Include code examples and practical examples when relevant.\n");
        }
        if (preferences.getIncludeTimeline()) {
            sb.append("- Include time estimates for learning goals when discussing roadmaps.\n");
        }

        sb.append("\n");
        return sb.toString();
    }

    /**
     * Build conversation history section with recent messages
     */
    private String buildConversationHistorySection(List<ChatMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## Conversation History\n\n");

        conversationHistory.forEach(msg -> {
            String roleName = msg.getRole() == MessageRole.USER ? "User" : "Assistant";
            sb.append(String.format("**%s**: %s\n\n", roleName, msg.getContent()));
        });

        return sb.toString();
    }

    /**
     * Build user context section based on context level preference
     */
    private String buildUserContextSection(User user, UserAIPreferences preferences) {
        ContextLevel contextLevel = preferences.getContextLevel();

        if (contextLevel == ContextLevel.MINIMAL) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("## User Context\n\n");

        // BALANCED and DEEP levels include basic user info
        if (contextLevel == ContextLevel.BALANCED || contextLevel == ContextLevel.DEEP) {
            sb.append(buildBasicUserContext(user, preferences));
        }

        // DEEP level includes complete profile
        if (contextLevel == ContextLevel.DEEP) {
            sb.append(buildDeepUserContext(user));
        }

        return sb.toString();
    }

    /**
     * Build basic user context (name, current role, key skills)
     */
    private String buildBasicUserContext(User user, UserAIPreferences preferences) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("**Name**: %s\n", user.getName()));

        if (user.getCurrentRole() != null) {
            sb.append(String.format("**Current Role**: %s\n", user.getCurrentRole()));
        }

        if (user.getTargetRole() != null) {
            sb.append(String.format("**Target Role**: %s\n", user.getTargetRole()));
        }

        if (user.getExperienceLevel() != null) {
            sb.append(String.format("**Experience Level**: %s\n", user.getExperienceLevel()));
        }

        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            String skillsList = String.join(", ", user.getSkills());
            sb.append(String.format("**Skills**: %s\n", skillsList));
        }

        if (user.getGoals() != null && !user.getGoals().isEmpty()) {
            sb.append(String.format("**Goals**: %s\n", user.getGoals()));
        }

        sb.append("\n");
        return sb.toString();
    }

    /**
     * Build deep user context (complete profile with skills and recommendations)
     * This is for DEEP context level
     */
    private String buildDeepUserContext(User user) {
        StringBuilder sb = new StringBuilder();

        // This would include skills, recommendations, and other detailed profile data
        // For now, we include a placeholder - in production this would fetch from database
        sb.append("**User Skills**: [Skills would be loaded from profile]\n");
        sb.append("**Skill Gaps**: [Identified gaps would be shown here]\n");
        sb.append("**Recommendations**: [Personalized recommendations based on analysis]\n\n");

        return sb.toString();
    }

    /**
     * Format conversation history as markdown for better readability
     */
    public String formatConversationHistory(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No previous conversation history.";
        }

        return messages.stream()
                .map(msg -> String.format("**%s**: %s",
                        msg.getRole() == MessageRole.USER ? "User" : "Assistant",
                        msg.getContent()))
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Build a summary prompt for the AI when context is too long
     * This is used before conversation summarization
     */
    public String buildSummarizationPrompt(List<ChatMessage> conversationHistory) {
        return "Please summarize the following conversation, capturing the main topics, " +
                "key points discussed, and any conclusions reached. " +
                "Keep the summary concise but comprehensive:\n\n" +
                formatConversationHistory(conversationHistory);
    }
}
