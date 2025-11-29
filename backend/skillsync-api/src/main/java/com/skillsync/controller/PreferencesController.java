package com.skillsync.controller;

import com.skillsync.dto.preferences.UserAIPreferencesDTO;
import com.skillsync.service.AIPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * PreferencesController
 * REST API endpoints for user AI preferences
 *
 * Endpoints:
 * - GET /api/preferences - Get user's preferences
 * - POST /api/preferences - Save/update preferences
 * - POST /api/preferences/preset/{name} - Apply preset
 * - POST /api/preferences/reset - Reset to defaults
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/preferences")
@RequiredArgsConstructor
public class PreferencesController {

    private final AIPreferencesService preferencesService;

    /**
     * Get user's AI preferences
     * Returns defaults if user hasn't customized yet
     */
    @GetMapping
    public ResponseEntity<UserAIPreferencesDTO> getUserPreferences(
            @RequestAttribute("userId") UUID userId) {
        log.debug("Fetching AI preferences for user: {}", userId);

        UserAIPreferencesDTO prefs = preferencesService.getUserPreferences(userId);
        return ResponseEntity.ok(prefs);
    }

    /**
     * Save or update user preferences
     */
    @PostMapping
    public ResponseEntity<UserAIPreferencesDTO> saveUserPreferences(
            @RequestBody UserAIPreferencesDTO preferencesDTO,
            @RequestAttribute("userId") UUID userId) {
        log.info("Saving AI preferences for user: {}", userId);

        if (preferencesDTO == null) {
            return ResponseEntity.badRequest().build();
        }

        UserAIPreferencesDTO saved = preferencesService.saveUserPreferences(userId, preferencesDTO);
        return ResponseEntity.ok(saved);
    }

    /**
     * Apply a preset configuration
     * Available presets: study_mode, interview_prep, career_mentor, casual_advisor
     */
    @PostMapping("/preset/{presetName}")
    public ResponseEntity<UserAIPreferencesDTO> applyPreset(
            @PathVariable String presetName,
            @RequestAttribute("userId") UUID userId) {
        log.info("Applying preset '{}' for user: {}", presetName, userId);

        try {
            UserAIPreferencesDTO result = preferencesService.applyPreset(userId, presetName);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid preset name: {}", presetName);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Reset preferences to defaults
     */
    @PostMapping("/reset")
    public ResponseEntity<UserAIPreferencesDTO> resetToDefaults(
            @RequestAttribute("userId") UUID userId) {
        log.info("Resetting preferences to defaults for user: {}", userId);

        UserAIPreferencesDTO result = preferencesService.resetToDefaults(userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get available presets
     */
    @GetMapping("/presets")
    public ResponseEntity<PresetInfo[]> getAvailablePresets() {
        log.debug("Fetching available presets");

        PresetInfo[] presets = new PresetInfo[]{
                PresetInfo.builder()
                        .name("study_mode")
                        .description("Optimized for learning and studying. BALANCED context, DETAILED responses, MENTORING tone")
                        .contextLevel("BALANCED")
                        .responseStyle("DETAILED")
                        .tone("MENTORING")
                        .build(),
                PresetInfo.builder()
                        .name("interview_prep")
                        .description("Prepare for interviews. DEEP context, DETAILED responses, PROFESSIONAL tone")
                        .contextLevel("DEEP")
                        .responseStyle("DETAILED")
                        .tone("PROFESSIONAL")
                        .build(),
                PresetInfo.builder()
                        .name("career_mentor")
                        .description("Career guidance and mentoring. BALANCED context, BALANCED responses, MENTORING tone")
                        .contextLevel("BALANCED")
                        .responseStyle("BALANCED")
                        .tone("MENTORING")
                        .build(),
                PresetInfo.builder()
                        .name("casual_advisor")
                        .description("Casual and friendly advice. MINIMAL context, CONCISE responses, CASUAL tone")
                        .contextLevel("MINIMAL")
                        .responseStyle("CONCISE")
                        .tone("CASUAL")
                        .build()
        };

        return ResponseEntity.ok(presets);
    }

    /**
     * DTO for preset information
     */
    @lombok.Data
    @lombok.Builder
    public static class PresetInfo {
        private String name;
        private String description;
        private String contextLevel;
        private String responseStyle;
        private String tone;
    }
}
