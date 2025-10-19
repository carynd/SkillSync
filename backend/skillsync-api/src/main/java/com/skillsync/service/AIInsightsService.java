package com.skillsync.service;

import com.skillsync.dto.ai.CareerAdviceRequest;
import com.skillsync.dto.ai.CareerAdviceResponse;
import com.skillsync.dto.ai.SkillGapRequest;
import com.skillsync.dto.ai.UserProfileRequest;
import com.skillsync.model.User;
import com.skillsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIInsightsService {

    private final WebClient.Builder webClientBuilder;
    private final UserRepository userRepository;
    private final RecommendationService recommendationService;

    @Value("${ai.service.url:http://localhost:8001}")
    private String aiServiceUrl;

    @Value("${ai.service.timeout:30000}")
    private long timeout;

    public Mono<CareerAdviceResponse> getCareerAdvice(UUID userId, String question) {
        log.info("Requesting career advice for user: {}", userId);

        // Get user profile
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get skill gap analysis from recommendations
        var recommendation = recommendationService.getRecommendations(userId);

        // Build request
        // Convert ExperienceLevel enum to years for AI service
        int experienceYears = 0;
        if (user.getExperienceLevel() != null) {
            switch (user.getExperienceLevel()) {
                case BEGINNER: experienceYears = 1; break;
                case INTERMEDIATE: experienceYears = 4; break;
                case ADVANCED: experienceYears = 8; break;
                case EXPERT: experienceYears = 12; break;
            }
        }

        UserProfileRequest userProfile = UserProfileRequest.builder()
                .userId(userId.toString())
                .name(user.getName())
                .currentRole(user.getCurrentRole() != null ? user.getCurrentRole() : "Developer")
                .targetRole(recommendation.getTargetRole())
                .currentSkills(user.getSkills() != null ? user.getSkills() : new ArrayList<>())
                .experienceYears(experienceYears)
                .build();

        // Extract missing skills from recommendations
        List<String> missingSkills = recommendation.getRecommendations()
                .stream()
                .map(rec -> rec.getSkillName())
                .toList();

        SkillGapRequest skillGap = SkillGapRequest.builder()
                .skillGapPercentage(recommendation.getSkillGapPercentage())
                .alignmentScore(recommendation.getAlignmentScore())
                .missingSkills(missingSkills)
                .matchingSkillsCount(recommendation.getMatchingSkillsCount())
                .missingSkillsCount(recommendation.getMissingSkillsCount())
                .build();

        CareerAdviceRequest request = CareerAdviceRequest.builder()
                .userProfile(userProfile)
                .skillGap(skillGap)
                .question(question)
                .build();

        // Call AI service
        WebClient webClient = webClientBuilder
                .baseUrl(aiServiceUrl)
                .build();

        return webClient.post()
                .uri("/api/ai/advice")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CareerAdviceResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> log.info("Received AI advice for user: {}", userId))
                .doOnError(error -> log.error("Error getting AI advice for user {}: {}", userId, error.getMessage()));
    }

    public Mono<String> getAIServiceHealth() {
        WebClient webClient = webClientBuilder
                .baseUrl(aiServiceUrl)
                .build();

        return webClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofMillis(5000))
                .onErrorReturn("AI Service Unavailable");
    }
}
