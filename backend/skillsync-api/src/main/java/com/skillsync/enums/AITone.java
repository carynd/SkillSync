package com.skillsync.enums;

/**
 * AI Tone Enum
 * Controls the personality, formality, and style of AI responses
 *
 * Design: Tone is injected into system prompt for Gemini
 *
 * @author SkillSync Team
 * @version 1.0
 */
public enum AITone {
    /**
     * Professional tone
     * Formal, direct, business-like language
     * Use case: Corporate professionals, formal career discussions
     */
    PROFESSIONAL(
        "PROFESSIONAL",
        "Formal, direct, business-like",
        (short) 1,
        "Keep responses professional and concise. Use formal language. " +
        "Focus on facts and actionable steps. Avoid casual expressions."
    ),

    /**
     * Casual tone
     * Friendly, conversational, approachable language
     * Use case: Students, informal learning, friendly interaction
     */
    CASUAL(
        "CASUAL",
        "Friendly, conversational, approachable",
        (short) 2,
        "Be friendly and conversational. Use casual language and relatable examples. " +
        "Feel free to use humor. Make the conversation feel natural and approachable."
    ),

    /**
     * Mentoring tone
     * Encouraging, explanatory, supportive language
     * Use case: Learning encouragement, growth mindset, career mentoring
     * Default for most users
     */
    MENTORING(
        "MENTORING",
        "Encouraging, explanatory, supportive",
        (short) 3,
        "Be a supportive mentor. Encourage learning and growth. Explain concepts clearly. " +
        "Celebrate progress. Be patient and understanding. Use a warm, encouraging tone."
    );

    private final String toneName;
    private final String description;
    private final short databaseId;
    private final String systemPromptModifier;

    AITone(String toneName, String description, short databaseId, String systemPromptModifier) {
        this.toneName = toneName;
        this.description = description;
        this.databaseId = databaseId;
        this.systemPromptModifier = systemPromptModifier;
    }

    public short getDatabaseId() {
        return databaseId;
    }

    public String getToneName() {
        return toneName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get the system prompt modifier for this tone
     * This is injected into the Gemini system prompt
     */
    public String getSystemPromptModifier() {
        return systemPromptModifier;
    }

    public static AITone fromDatabaseId(short id) {
        for (AITone tone : AITone.values()) {
            if (tone.databaseId == id) {
                return tone;
            }
        }
        throw new IllegalArgumentException("Unknown AI tone ID: " + id);
    }

    public static AITone fromString(String toneName) {
        try {
            return AITone.valueOf(toneName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown AI tone: " + toneName, e);
        }
    }

    /**
     * Get default tone for new users
     */
    public static AITone getDefault() {
        return MENTORING;
    }
}
