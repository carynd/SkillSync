package com.skillsync.controller;

import com.skillsync.dto.RecommendationResponse;
import com.skillsync.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Slf4j
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * Generate new recommendations for a user
     * POST /api/recommendations/generate/{userId}
     */
    @PostMapping("/generate/{userId}")
    public ResponseEntity<RecommendationResponse> generateRecommendations(@PathVariable UUID userId) {
        log.info("Generating recommendations for user: {}", userId);
        RecommendationResponse response = recommendationService.generateRecommendations(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get existing recommendations for a user (from cache)
     * GET /api/recommendations/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<RecommendationResponse> getRecommendations(@PathVariable UUID userId) {
        log.info("Fetching recommendations for user: {}", userId);
        try {
            RecommendationResponse response = recommendationService.getRecommendations(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.warn("No cached recommendations found for user: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Clear cached recommendations for a user
     * DELETE /api/recommendations/{userId}
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearRecommendations(@PathVariable UUID userId) {
        log.info("Clearing recommendations for user: {}", userId);
        recommendationService.clearRecommendations(userId);
        return ResponseEntity.noContent().build();
    }
}
