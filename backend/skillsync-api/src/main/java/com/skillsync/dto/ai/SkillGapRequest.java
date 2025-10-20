package com.skillsync.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapRequest {
    @JsonProperty("skill_gap_percentage")
    private Double skillGapPercentage;

    @JsonProperty("alignment_score")
    private Double alignmentScore;

    @JsonProperty("missing_skills")
    private List<String> missingSkills;

    @JsonProperty("matching_skills_count")
    private Integer matchingSkillsCount;

    @JsonProperty("missing_skills_count")
    private Integer missingSkillsCount;
}
