package com.skillsync.service;

import com.skillsync.entity.ChatMessage;
import com.skillsync.entity.UserAIPreferences;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * GeminiAIService
 * Handles all communication with Google Gemini API
 *
 * Responsibilities:
 * - Send context and messages to Gemini
 * - Handle Gemini responses
 * - Implement graceful fallback when Gemini is unavailable
 * - Track token usage for quota/billing
 * - Build rich prompts with user context and preferences
 *
 * @author SkillSync Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAIService {

    @Value("${gemini.api.url:http://localhost:8001}")
    private String geminiApiUrl;

    @Value("${gemini.api.timeout:30000}")
    private int geminiTimeout;

    private final RestTemplate restTemplate;
    private final ChatMessageService messageService;
    private final AIContextBuilder contextBuilder;

    /**
     * Send a message to Gemini AI and get response
     * Includes graceful fallback if Gemini is unavailable
     */
    public GeminiResponse askGemini(String conversationId, String userMessage,
                                    String context, UserAIPreferences preferences) {
        log.info("Sending message to Gemini for conversation: {}", conversationId);

        try {
            // Build the request for Gemini
            Map<String, Object> request = buildGeminiRequest(userMessage, context, preferences);

            log.debug("Sending request to Gemini API: {}", geminiApiUrl);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    geminiApiUrl + "/api/career-advice",
                    HttpMethod.POST,
                    new HttpEntity<>(request, getHeaders()),
                    (Class<Map<String, Object>>) (Class<?>) Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String aiResponse = (String) body.get("response");
                Object tokenObj = body.getOrDefault("token_count", 0);
                Integer tokenCount = tokenObj instanceof Integer ? (Integer) tokenObj :
                        (tokenObj instanceof Number ? ((Number) tokenObj).intValue() : 0);
                String finishReason = (String) body.getOrDefault("finish_reason", "STOP");

                log.info("Received response from Gemini");
                return GeminiResponse.builder()
                        .success(true)
                        .content(aiResponse)
                        .tokenCount(tokenCount)
                        .finishReason(finishReason)
                        .isCached(false)
                        .errorMessage(null)
                        .build();
            }

            log.warn("Unexpected response from Gemini: {}", response.getStatusCode());
            return provideFallbackResponse(conversationId);

        } catch (RestClientException e) {
            log.error("Failed to contact Gemini API: {}", e.getMessage());
            return provideFallbackResponse(conversationId);
        } catch (Exception e) {
            log.error("Unexpected error communicating with Gemini", e);
            return provideFallbackResponse(conversationId);
        }
    }

    /**
     * Provide graceful fallback response when Gemini is unavailable
     * Strategy: Use cached response or provide helpful message
     */
    private GeminiResponse provideFallbackResponse(String conversationId) {
        log.warn("Providing fallback response for conversation: {}", conversationId);

        // Try to find a cached response from previous AI responses
        java.util.UUID convId;
        try {
            convId = java.util.UUID.fromString(conversationId);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid conversation ID format: {}", conversationId);
            convId = null;
        }

        Optional<ChatMessage> cachedResponse = convId != null ? messageService.getCachedResponse(convId) : Optional.empty();

        if (cachedResponse.isPresent()) {
            log.info("Using cached response for fallback");
            return GeminiResponse.builder()
                    .success(true)
                    .content(cachedResponse.get().getContent())
                    .tokenCount(cachedResponse.get().getTokenCount())
                    .finishReason("CACHED")
                    .isCached(true)
                    .errorMessage("Using cached response (AI service unavailable)")
                    .build();
        }

        // No cached response, provide helpful fallback message
        String fallbackMessage = """
                I'm temporarily unavailable right now, but I want to help you!

                Here are some things you can do:
                1. **Review our resources**: Check out the learning resources in your dashboard
                2. **Revisit past conversations**: Your previous advice is saved here
                3. **Try again later**: I'll be back shortly

                In the meantime, feel free to prepare questions about:
                • Your career goals and aspirations
                • Skills you'd like to develop
                • Specific job roles you're targeting
                • Interview preparation

                Your message has been saved and I'll be ready to help as soon as I'm back!
                """;

        return GeminiResponse.builder()
                .success(false)
                .content(fallbackMessage)
                .tokenCount(fallbackMessage.length() / 4)
                .finishReason("UNAVAILABLE")
                .isCached(false)
                .errorMessage("AI service temporarily unavailable. Showing helpful guidance.")
                .build();
    }

    /**
     * Build request payload for Gemini API
     */
    private Map<String, Object> buildGeminiRequest(String userMessage, String context,
                                                     UserAIPreferences preferences) {
        Map<String, Object> request = new HashMap<>();

        // User message
        request.put("message", userMessage);

        // Rich context respecting preferences
        request.put("context", context);

        // User preferences for response customization
        Map<String, Object> prefsMap = new HashMap<>();
        prefsMap.put("context_level", preferences.getContextLevel().name());
        prefsMap.put("response_style", preferences.getResponseStyle().name());
        prefsMap.put("tone", preferences.getTone().name());
        prefsMap.put("include_resources", preferences.getIncludeResources());
        prefsMap.put("include_examples", preferences.getIncludeExamples());
        prefsMap.put("include_timeline", preferences.getIncludeTimeline());
        request.put("preferences", prefsMap);

        return request;
    }

    /**
     * Get HTTP headers for Gemini API
     */
    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "application/json");
        return headers;
    }

    /**
     * Response from Gemini API
     */
    @lombok.Data
    @lombok.Builder
    public static class GeminiResponse {
        private boolean success;
        private String content;
        private Integer tokenCount;
        private String finishReason;
        private boolean isCached;
        private String errorMessage;
    }
}
