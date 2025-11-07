package com.skillsync.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Plan Type Enum
 * Represents user subscription tier with feature access levels
 *
 * Design: Enum-based plans allow easy scaling without schema changes
 * New plan? Just add enum, no database migration needed
 *
 * @author SkillSync Team
 * @version 1.0
 */
public enum PlanType {
    /**
     * Free tier
     * Limited conversations (2 max)
     * Limited monthly messages (100)
     * Basic features only
     */
    FREE(
        "FREE",
        "Free tier with limited features",
        (short) 1,
        2,              // max conversations
        100,            // max messages per month
        new HashMap<String, Boolean>() {{
            put("include_resources", true);
            put("basic_preferences", true);
            put("unlimited_chats", false);
            put("priority_api", false);
            put("analytics", false);
        }}
    ),

    /**
     * Premium tier
     * More conversations (10 max)
     * More monthly messages (1000)
     * All features enabled
     */
    PREMIUM(
        "PREMIUM",
        "Premium tier with unlimited features",
        (short) 2,
        10,             // max conversations
        1000,           // max messages per month
        new HashMap<String, Boolean>() {{
            put("include_resources", true);
            put("basic_preferences", true);
            put("unlimited_chats", false);
            put("priority_api", false);
            put("advanced_customization", true);
            put("analytics", false);
        }}
    ),

    /**
     * Pro tier
     * Unlimited conversations
     * Unlimited monthly messages
     * All features + priority API access
     */
    PRO(
        "PRO",
        "Pro tier with all premium features and priority support",
        (short) 3,
        999,            // practically unlimited
        999999,         // practically unlimited
        new HashMap<String, Boolean>() {{
            put("include_resources", true);
            put("basic_preferences", true);
            put("unlimited_chats", true);
            put("priority_api", true);
            put("advanced_customization", true);
            put("analytics", true);
            put("export_conversations", true);
            put("custom_ai_presets", true);
        }}
    );

    private final String planName;
    private final String description;
    private final short databaseId;
    private final int maxConversations;
    private final int maxMessagesPerMonth;
    private final Map<String, Boolean> features;

    PlanType(String planName, String description, short databaseId,
             int maxConversations, int maxMessagesPerMonth,
             Map<String, Boolean> features) {
        this.planName = planName;
        this.description = description;
        this.databaseId = databaseId;
        this.maxConversations = maxConversations;
        this.maxMessagesPerMonth = maxMessagesPerMonth;
        this.features = new HashMap<>(features);  // Defensive copy
    }

    public short getDatabaseId() {
        return databaseId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get max conversations allowed for this plan
     */
    public int getMaxConversations() {
        return maxConversations;
    }

    /**
     * Get max messages allowed per month for this plan
     */
    public int getMaxMessagesPerMonth() {
        return maxMessagesPerMonth;
    }

    /**
     * Check if a feature is enabled for this plan
     */
    public boolean hasFeature(String featureName) {
        return features.getOrDefault(featureName, false);
    }

    /**
     * Get all features for this plan
     */
    public Map<String, Boolean> getFeatures() {
        return new HashMap<>(features);  // Defensive copy
    }

    public static PlanType fromDatabaseId(short id) {
        for (PlanType plan : PlanType.values()) {
            if (plan.databaseId == id) {
                return plan;
            }
        }
        throw new IllegalArgumentException("Unknown plan type ID: " + id);
    }

    public static PlanType fromString(String planName) {
        try {
            return PlanType.valueOf(planName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown plan type: " + planName, e);
        }
    }

    /**
     * Get default plan for new users
     */
    public static PlanType getDefault() {
        return FREE;
    }

    /**
     * Check if user can create new conversation
     */
    public boolean canCreateNewConversation(int currentConversationCount) {
        return currentConversationCount < maxConversations;
    }

    /**
     * Check if user exceeded message limit for this month
     */
    public boolean canSendMessage(int messagesUsedThisMonth) {
        return messagesUsedThisMonth < maxMessagesPerMonth;
    }
}
