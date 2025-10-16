package com.skillsync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private UUID userId;
    private String userName;
    private String currentRole;
    private String targetRole;
    private List<String> currentSkills;
    private Double skillGapPercentage;
    private Double alignmentScore;
    private Integer missingSkillsCount;
    private Integer matchingSkillsCount;
    private List<SkillRecommendation> recommendations;
    private LocalDateTime generatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillRecommendation {
        private String skillName;
        private String category;
        private Integer demandScore;
        private String priority;
        private String reason;
        private String estimatedLearningTime;
    }
}
