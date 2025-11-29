package com.skillsync.service;

import com.skillsync.dto.preferences.UserAIPreferencesDTO;
import com.skillsync.entity.UserAIPreferences;
import com.skillsync.enums.AITone;
import com.skillsync.enums.ContextLevel;
import com.skillsync.enums.ResponseStyle;
import com.skillsync.repository.UserAIPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * AIPreferencesService
 * Manages user AI customization preferences and presets
 *
 * Responsibilities:
 * - Load/save user preferences from database
 * - Apply preset configurations
 * - Record preference usage for analytics
 * - Provide defaults for new users
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIPreferencesService {

    private final UserAIPreferencesRepository preferencesRepository;

    /**
     * Get preferences for a user, creating defaults if they don't exist
     */
    @Transactional(readOnly = true)
    public UserAIPreferencesDTO getUserPreferences(UUID userId) {
        log.debug("Fetching AI preferences for user: {}", userId);

        UserAIPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        return UserAIPreferencesDTO.fromEntity(prefs);
    }

    /**
     * Get preferences entity (for internal use in context building)
     */
    @Transactional(readOnly = true)
    public UserAIPreferences getUserPreferencesEntity(UUID userId) {
        return preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
    }

    /**
     * Save or update user preferences
     */
    @Transactional
    public UserAIPreferencesDTO saveUserPreferences(UUID userId, UserAIPreferencesDTO preferencesDTO) {
        log.info("Saving AI preferences for user: {}", userId);

        UserAIPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> UserAIPreferences.builder()
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build());

        // Update from DTO
        preferencesDTO.mergeIntoEntity(prefs);
        prefs.setUpdatedAt(LocalDateTime.now());

        UserAIPreferences saved = preferencesRepository.save(prefs);
        log.info("Preferences saved for user: {}", userId);

        return UserAIPreferencesDTO.fromEntity(saved);
    }

    /**
     * Apply a preset configuration to user's preferences
     * Presets: study_mode, interview_prep, career_mentor, casual_advisor
     */
    @Transactional
    public UserAIPreferencesDTO applyPreset(UUID userId, String presetName) {
        log.info("Applying preset '{}' to user: {}", presetName, userId);

        UserAIPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        prefs.applyPreset(presetName);
        prefs.setUpdatedAt(LocalDateTime.now());

        UserAIPreferences saved = preferencesRepository.save(prefs);
        log.info("Preset '{}' applied for user: {}", presetName, userId);

        return UserAIPreferencesDTO.fromEntity(saved);
    }

    /**
     * Record that preferences were used (for analytics)
     */
    @Transactional
    public void recordPreferenceUsage(UUID userId) {
        preferencesRepository.findByUserId(userId).ifPresent(prefs -> {
            prefs.setLastUsedAt(LocalDateTime.now());
            preferencesRepository.save(prefs);
        });
    }

    /**
     * Reset preferences to defaults
     */
    @Transactional
    public UserAIPreferencesDTO resetToDefaults(UUID userId) {
        log.info("Resetting preferences to defaults for user: {}", userId);

        UserAIPreferences prefs = preferencesRepository.findByUserId(userId)
                .orElseGet(() -> UserAIPreferences.builder()
                        .userId(userId)
                        .createdAt(LocalDateTime.now())
                        .build());

        prefs.resetToDefaults();
        prefs.setUpdatedAt(LocalDateTime.now());

        UserAIPreferences saved = preferencesRepository.save(prefs);
        return UserAIPreferencesDTO.fromEntity(saved);
    }

    /**
     * Create default preferences for a new user
     * Defaults are psychological: BALANCED context, BALANCED response, MENTORING tone
     */
    private UserAIPreferences createDefaultPreferences(UUID userId) {
        log.info("Creating default preferences for new user: {}", userId);

        UserAIPreferences prefs = UserAIPreferences.builder()
                .userId(userId)
                .contextLevel(ContextLevel.BALANCED)
                .responseStyle(ResponseStyle.BALANCED)
                .tone(AITone.MENTORING)
                .includeResources(true)
                .includeExamples(true)
                .includeTimeline(true)
                .includeUserContext(true)
                .showCustomizeButton(true)
                .showTipsOnFirstMessages(true)
                .autoSummarizeConversations(true)
                .createdAt(LocalDateTime.now())
                .build();

        return preferencesRepository.save(prefs);
    }
}
