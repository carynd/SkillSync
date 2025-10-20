package com.skillsync.controller;

import com.skillsync.dto.ai.CareerAdviceResponse;
import com.skillsync.service.AIInsightsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIInsightsController {

    private final AIInsightsService aiInsightsService;

    /**
     * Get AI-powered career advice for a user
     * POST /api/ai/advice/{userId}
     */
    @PostMapping("/advice/{userId}")
    public Mono<ResponseEntity<CareerAdviceResponse>> getCareerAdvice(
            @PathVariable UUID userId,
            @RequestBody Map<String, String> request) {

        log.info("Received career advice request for user: {}", userId);
        String question = request.getOrDefault("question", "What should I focus on to reach my career goals?");

        return aiInsightsService.getCareerAdvice(userId, question)
                .map(ResponseEntity::ok)
                .doOnError(error -> log.error("Error getting AI advice for user {}: {}", userId, error.getMessage(), error))
                .onErrorResume(error -> {
                    log.error("Failed to get AI advice: {}", error.getMessage());
                    return Mono.just(ResponseEntity.internalServerError().build());
                });
    }

    /**
     * Check AI service health
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> checkAIServiceHealth() {
        return aiInsightsService.getAIServiceHealth()
                .map(status -> {
                    Map<String, Object> response = Map.of(
                            "service", "AI Insights",
                            "status", status,
                            "endpoint", "http://localhost:8001"
                    );
                    return ResponseEntity.ok(response);
                })
                .onErrorReturn(ResponseEntity.ok(Map.of(
                        "service", (Object) "AI Insights",
                        "status", (Object) "unavailable",
                        "endpoint", (Object) "http://localhost:8001"
                )));
    }
}
