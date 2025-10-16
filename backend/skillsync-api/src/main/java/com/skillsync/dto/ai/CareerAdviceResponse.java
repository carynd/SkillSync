package com.skillsync.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerAdviceResponse {
    private String advice;
    private String reasoning;

    @JsonProperty("action_items")
    private List<String> actionItems;

    @JsonProperty("estimated_timeline")
    private String estimatedTimeline;

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @JsonProperty("generated_at")
    private LocalDateTime generatedAt;
}
