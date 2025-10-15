package com.skillsync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private UUID recommendationId;
    private UUID userId;
    private List<String> missingSkills;
    private Map<String, List<LearningResource>> resources;
    private Double skillGapPercentage;
    private Double alignmentScore;
    private String summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LearningResource {
        private String title;
        private String url;
        private String platform; // e.g., "Coursera", "YouTube", "Udemy"
        private String type; // e.g., "Course", "Tutorial", "Documentation"
        private Integer duration; // in hours
        private String difficulty; // "Beginner", "Intermediate", "Advanced"
    }
}
