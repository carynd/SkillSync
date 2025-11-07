package com.skillsync.enums;

/**
 * Response Style Enum
 * Controls verbosity, depth, and length of AI responses
 *
 * Design: Token limits prevent cost overruns and user attention fatigue
 *
 * @author SkillSync Team
 * @version 1.0
 */
public enum ResponseStyle {
    /**
     * Concise - Short, direct answers
     * Token limit: 500 tokens max (~200 words)
     * Use case: Quick advice, busy professionals
     */
    CONCISE("CONCISE", "Short, direct answers (2-3 paragraphs)", (short) 1, 500),

    /**
     * Balanced - Medium-length, comprehensive
     * Token limit: 1000 tokens max (~400 words)
     * Use case: Standard advice with enough detail
     * Default choice for most users
     */
    BALANCED("BALANCED", "Medium-length, comprehensive (5-7 paragraphs)", (short) 2, 1000),

    /**
     * Detailed - Comprehensive, covers all angles
     * Token limit: 1500 tokens max (~600 words)
     * Use case: Deep dives, career path planning, learning roadmaps
     */
    DETAILED("DETAILED", "Comprehensive, all angles covered", (short) 3, 1500);

    private final String styleName;
    private final String description;
    private final short databaseId;
    private final int maxTokens;

    ResponseStyle(String styleName, String description, short databaseId, int maxTokens) {
        this.styleName = styleName;
        this.description = description;
        this.databaseId = databaseId;
        this.maxTokens = maxTokens;
    }

    public short getDatabaseId() {
        return databaseId;
    }

    public String getStyleName() {
        return styleName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the maximum token limit for this style
     * Used to configure Gemini API generation config
     */
    public int getMaxTokens() {
        return maxTokens;
    }

    public static ResponseStyle fromDatabaseId(short id) {
        for (ResponseStyle style : ResponseStyle.values()) {
            if (style.databaseId == id) {
                return style;
            }
        }
        throw new IllegalArgumentException("Unknown response style ID: " + id);
    }

    public static ResponseStyle fromString(String styleName) {
        try {
            return ResponseStyle.valueOf(styleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown response style: " + styleName, e);
        }
    }

    /**
     * Get default response style for new users
     */
    public static ResponseStyle getDefault() {
        return BALANCED;
    }
}
