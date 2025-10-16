package com.skillsync.service;

import com.skillsync.dto.RecommendationResponse;
import com.skillsync.dto.SkillGapAnalysis;
import com.skillsync.model.Recommendation;
import com.skillsync.model.SkillDemand;
import com.skillsync.model.User;
import com.skillsync.repository.RecommendationRepository;
import com.skillsync.repository.SkillDemandRepository;
import com.skillsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    private final UserRepository userRepository;
    private final SkillDemandRepository skillDemandRepository;
    private final RecommendationRepository recommendationRepository;

    private static final int RECOMMENDATION_CACHE_HOURS = 24;

    /**
     * Generate personalized recommendations for a user based on their target role
     */
    @Transactional
    public RecommendationResponse generateRecommendations(UUID userId) {
        log.info("Generating recommendations for user: {}", userId);

        // 1. Get user profile
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        // 2. Validate user has required data
        if (user.getTargetRole() == null || user.getTargetRole().isEmpty()) {
            throw new RuntimeException("User must set a target role first");
        }

        // 3. Check for cached recommendations
        Optional<Recommendation> cachedRecommendation = recommendationRepository
                .findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());

        if (cachedRecommendation.isPresent()) {
            log.info("Returning cached recommendations for user: {}", userId);
            return buildRecommendationResponse(user, cachedRecommendation.get());
        }

        // 4. Get skills required for target role
        List<SkillDemand> requiredSkills = skillDemandRepository
                .findByRoleOrderByDemandScoreDesc(user.getTargetRole());

        if (requiredSkills.isEmpty()) {
            throw new RuntimeException("No skill data available for role: " + user.getTargetRole());
        }

        // 5. Get user's current skills
        Set<String> userSkills = user.getSkills() != null ?
                new HashSet<>(user.getSkills()) : new HashSet<>();

        // 6. Calculate skill gap
        SkillGapAnalysis gap = calculateSkillGap(userSkills, requiredSkills);

        // 7. Identify missing skills with priorities
        List<RecommendationResponse.SkillRecommendation> recommendations =
                generateSkillRecommendations(gap.getMissingSkills(), requiredSkills);

        // 8. Generate learning resources
        String resources = generateLearningResources(recommendations);

        // 9. Save recommendation to cache
        Recommendation recommendation = Recommendation.builder()
                .userId(userId)
                .targetRole(user.getTargetRole())
                .missingSkills(gap.getMissingSkills())
                .skillGapPercentage(gap.getGapPercentage())
                .alignmentScore(gap.getAlignmentScore())
                .resources(resources)
                .expiresAt(LocalDateTime.now().plusHours(RECOMMENDATION_CACHE_HOURS))
                .build();

        recommendationRepository.save(recommendation);
        log.info("Saved recommendations to cache for user: {}", userId);

        // 10. Build and return response
        return RecommendationResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .currentRole(user.getCurrentRole())
                .targetRole(user.getTargetRole())
                .currentSkills(new ArrayList<>(userSkills))
                .skillGapPercentage(gap.getGapPercentage())
                .alignmentScore(gap.getAlignmentScore())
                .missingSkillsCount(gap.getMissingSkills().size())
                .matchingSkillsCount(gap.getMatchingSkills().size())
                .recommendations(recommendations)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculate skill gap between user skills and required skills
     */
    private SkillGapAnalysis calculateSkillGap(Set<String> userSkills, List<SkillDemand> requiredSkills) {
        Set<String> requiredSkillNames = requiredSkills.stream()
                .map(SkillDemand::getSkill)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> normalizedUserSkills = userSkills.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        // Find matching skills
        Set<String> matchingSkills = new HashSet<>(normalizedUserSkills);
        matchingSkills.retainAll(requiredSkillNames);

        // Find missing skills
        Set<String> missingSkills = new HashSet<>(requiredSkillNames);
        missingSkills.removeAll(normalizedUserSkills);

        // Calculate percentages
        int totalRequired = requiredSkillNames.size();
        int matched = matchingSkills.size();
        int missing = missingSkills.size();

        double alignmentScore = totalRequired > 0 ? (matched * 100.0 / totalRequired) : 0.0;
        double gapPercentage = totalRequired > 0 ? (missing * 100.0 / totalRequired) : 0.0;

        log.info("Skill Gap Analysis - Total: {}, Matched: {}, Missing: {}, Alignment: {}%, Gap: {}%",
                totalRequired, matched, missing, String.format("%.1f", alignmentScore), String.format("%.1f", gapPercentage));

        return SkillGapAnalysis.builder()
                .matchingSkills(new ArrayList<>(matchingSkills))
                .missingSkills(new ArrayList<>(missingSkills))
                .alignmentScore(alignmentScore)
                .gapPercentage(gapPercentage)
                .totalRequired(totalRequired)
                .totalMatched(matched)
                .totalMissing(missing)
                .build();
    }

    /**
     * Generate prioritized skill recommendations
     */
    private List<RecommendationResponse.SkillRecommendation> generateSkillRecommendations(
            List<String> missingSkills, List<SkillDemand> allSkillDemands) {

        // Create a map for quick lookup
        Map<String, SkillDemand> skillDemandMap = allSkillDemands.stream()
                .collect(Collectors.toMap(
                        sd -> sd.getSkill().toLowerCase(),
                        sd -> sd
                ));

        return missingSkills.stream()
                .map(skillName -> {
                    SkillDemand demand = skillDemandMap.get(skillName.toLowerCase());
                    if (demand == null) {
                        return null; // Skip if not found
                    }

                    String priority = determinePriority(demand.getDemandScore());
                    String reason = generateReason(demand);

                    return RecommendationResponse.SkillRecommendation.builder()
                            .skillName(demand.getSkill())
                            .category(demand.getCategory())
                            .demandScore(demand.getDemandScore())
                            .priority(priority)
                            .reason(reason)
                            .estimatedLearningTime(estimateLearningTime(demand.getCategory()))
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(r -> {
                    // Sort by priority: High -> Medium -> Low
                    switch (r.getPriority()) {
                        case "High": return 0;
                        case "Medium": return 1;
                        case "Low": return 2;
                        default: return 3;
                    }
                }))
                .collect(Collectors.toList());
    }

    /**
     * Determine priority based on demand score
     */
    private String determinePriority(Integer demandScore) {
        if (demandScore >= 80) {
            return "High";
        } else if (demandScore >= 60) {
            return "Medium";
        } else {
            return "Low";
        }
    }

    /**
     * Generate reason for learning this skill
     */
    private String generateReason(SkillDemand demand) {
        String priority = determinePriority(demand.getDemandScore());

        if ("High".equals(priority)) {
            return String.format("Essential skill with %d%% market demand. Highly sought after by employers.",
                    demand.getDemandScore());
        } else if ("Medium".equals(priority)) {
            return String.format("Important skill with %d%% market demand. Will strengthen your profile.",
                    demand.getDemandScore());
        } else {
            return String.format("Useful skill with %d%% market demand. Good to have for completeness.",
                    demand.getDemandScore());
        }
    }

    /**
     * Estimate learning time based on category
     */
    private String estimateLearningTime(String category) {
        switch (category.toLowerCase()) {
            case "programming language":
                return "8-12 weeks";
            case "framework":
                return "4-6 weeks";
            case "database":
                return "3-4 weeks";
            case "cloud":
                return "6-8 weeks";
            case "devops":
                return "4-6 weeks";
            case "tool":
                return "1-2 weeks";
            case "architecture":
                return "6-8 weeks";
            case "domain":
                return "8-12 weeks";
            case "library":
                return "2-3 weeks";
            default:
                return "4-6 weeks";
        }
    }

    /**
     * Generate learning resources (JSON format for flexibility)
     */
    private String generateLearningResources(List<RecommendationResponse.SkillRecommendation> recommendations) {
        // For now, return a simple JSON structure
        // In the future, this can fetch real resources from external APIs
        return "{}"; // Placeholder for future resource integration
    }

    /**
     * Build response from cached recommendation
     */
    private RecommendationResponse buildRecommendationResponse(User user, Recommendation cached) {
        // Get required skills for recommendations
        List<SkillDemand> requiredSkills = skillDemandRepository
                .findByRoleOrderByDemandScoreDesc(user.getTargetRole());

        List<RecommendationResponse.SkillRecommendation> recommendations =
                generateSkillRecommendations(cached.getMissingSkills(), requiredSkills);

        Set<String> userSkills = user.getSkills() != null ?
                new HashSet<>(user.getSkills()) : new HashSet<>();

        return RecommendationResponse.builder()
                .userId(user.getUserId())
                .userName(user.getName())
                .currentRole(user.getCurrentRole())
                .targetRole(user.getTargetRole())
                .currentSkills(new ArrayList<>(userSkills))
                .skillGapPercentage(cached.getSkillGapPercentage())
                .alignmentScore(cached.getAlignmentScore())
                .missingSkillsCount(cached.getMissingSkills().size())
                .matchingSkillsCount(userSkills.size() - cached.getMissingSkills().size())
                .recommendations(recommendations)
                .generatedAt(cached.getCreatedAt())
                .build();
    }

    /**
     * Get existing recommendations (from cache)
     */
    public RecommendationResponse getRecommendations(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Optional<Recommendation> cached = recommendationRepository
                .findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());

        if (cached.isEmpty()) {
            throw new RuntimeException("No recommendations found. Please generate recommendations first.");
        }

        return buildRecommendationResponse(user, cached.get());
    }

    /**
     * Clear cached recommendations for a user
     */
    @Transactional
    public void clearRecommendations(UUID userId) {
        recommendationRepository.deleteByUserId(userId);
        log.info("Cleared recommendations cache for user: {}", userId);
    }
}
