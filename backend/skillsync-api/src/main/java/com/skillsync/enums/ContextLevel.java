package com.skillsync.enums;

/**
 * Context Level Enum
 * Determines how deeply the AI references user's profile information
 *
 * Design Philosophy: Give users control over privacy/personalization tradeoff
 *
 * @author SkillSync Team
 * @version 1.0
 */
public enum ContextLevel {
    /**
     * Minimal context - No user data references
     * Privacy-first: AI doesn't know user's goals or skills
     * Use case: User wants anonymous advice
     */
    MINIMAL("MINIMAL", "No user data references", (short) 1),

    /**
     * Balanced context - Mention goals/skills when contextually relevant
     * Default: Most users want some personalization without oversharing
     * Use case: Standard learning advice
     */
    BALANCED("BALANCED", "Mention goals/skills when relevant", (short) 2),

    /**
     * Deep context - Actively reference profile, proactively suggest
     * Maximum personalization: AI remembers user's journey, makes suggestions
     * Use case: Career mentoring, detailed path planning
     */
    DEEP("DEEP", "Actively reference profile, proactively suggest", (short) 3);

    private final String levelName;
    private final String description;
    private final short databaseId;

    ContextLevel(String levelName, String description, short databaseId) {
        this.levelName = levelName;
        this.description = description;
        this.databaseId = databaseId;
    }

    public short getDatabaseId() {
        return databaseId;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if this level includes user context
     */
    public boolean includesUserContext() {
        return this != MINIMAL;
    }

    /**
     * Check if this level uses deep personalization
     */
    public boolean isDeepContext() {
        return this == DEEP;
    }

    public static ContextLevel fromDatabaseId(short id) {
        for (ContextLevel level : ContextLevel.values()) {
            if (level.databaseId == id) {
                return level;
            }
        }
        throw new IllegalArgumentException("Unknown context level ID: " + id);
    }

    public static ContextLevel fromString(String levelName) {
        try {
            return ContextLevel.valueOf(levelName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown context level: " + levelName, e);
        }
    }

    /**
     * Get default context level for new users
     */
    public static ContextLevel getDefault() {
        return BALANCED;
    }
}
