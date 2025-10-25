package com.skillsync.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.skillsync.enums.JobRole;

import java.io.IOException;

/**
 * Custom deserializer for JobRole enum.
 * Allows frontend to send display names (e.g., "Data Scientist")
 * and converts them to enum constants (e.g., DATA_SCIENTIST)
 */
public class JobRoleDeserializer extends JsonDeserializer<JobRole> {

    @Override
    public JobRole deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        String value = jsonParser.getValueAsString();

        if (value == null || value.isEmpty()) {
            return null;
        }

        // First, try to match by enum constant name (e.g., "DATA_SCIENTIST")
        try {
            return JobRole.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If that fails, try to match by display name (e.g., "Data Scientist")
            JobRole role = JobRole.fromDisplayName(value);
            if (role != null) {
                return role;
            }
        }

        // If no match found, throw error
        throw new IOException("Invalid JobRole value: " + value);
    }
}
