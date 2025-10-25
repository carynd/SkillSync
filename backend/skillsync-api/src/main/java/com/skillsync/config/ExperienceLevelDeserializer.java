package com.skillsync.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.skillsync.enums.ExperienceLevel;

import java.io.IOException;

/**
 * Custom deserializer for ExperienceLevel enum.
 * Allows frontend to send display names (e.g., "Intermediate")
 * and converts them to enum constants (e.g., INTERMEDIATE)
 */
public class ExperienceLevelDeserializer extends JsonDeserializer<ExperienceLevel> {

    @Override
    public ExperienceLevel deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        String value = jsonParser.getValueAsString();

        if (value == null || value.isEmpty()) {
            return null;
        }

        // First, try to match by enum constant name (e.g., "INTERMEDIATE")
        try {
            return ExperienceLevel.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If that fails, try to match by display name (e.g., "Intermediate")
            ExperienceLevel level = ExperienceLevel.fromDisplayName(value);
            if (level != null) {
                return level;
            }
        }

        // If no match found, throw error
        throw new IOException("Invalid ExperienceLevel value: " + value);
    }
}
