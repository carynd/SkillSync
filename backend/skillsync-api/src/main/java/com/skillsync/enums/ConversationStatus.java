package com.skillsync.enums;

/**
 * Conversation Status Enum
 * Represents the current state of a conversation
 *
 * Design Pattern: Enum instead of string literals
 * Benefits: Type-safe, IDE autocomplete, compile-time checking
 *
 * @author SkillSync Team
 * @version 1.0
 */
public enum ConversationStatus {
    /**
     * Conversation is currently active and user can interact with it
     */
    ACTIVE("ACTIVE", "Conversation is currently active", (short) 1),

    /**
     * Conversation has been archived by user but still accessible
     */
    ARCHIVED("ARCHIVED", "Conversation archived by user", (short) 2),

    /**
     * Conversation has been soft-deleted (not visible to user, but recoverable)
     */
    DELETED("DELETED", "Conversation soft-deleted", (short) 3),

    /**
     * Conversation is paused temporarily (user doesn't want to continue yet)
     */
    PAUSED("PAUSED", "Conversation paused temporarily", (short) 4);

    private final String statusName;
    private final String description;
    private final short databaseId;

    ConversationStatus(String statusName, String description, short databaseId) {
        this.statusName = statusName;
        this.description = description;
        this.databaseId = databaseId;
    }

    /**
     * Get the database ID for this status (for enum table lookups)
     */
    public short getDatabaseId() {
        return databaseId;
    }

    public String getStatusName() {
        return statusName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convert database ID to enum
     * Safer than ordinal() for database persistence
     */
    public static ConversationStatus fromDatabaseId(short id) {
        for (ConversationStatus status : ConversationStatus.values()) {
            if (status.databaseId == id) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown conversation status ID: " + id);
    }

    /**
     * Convert string to enum (case-insensitive)
     */
    public static ConversationStatus fromString(String statusName) {
        try {
            return ConversationStatus.valueOf(statusName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown conversation status: " + statusName, e);
        }
    }
}
