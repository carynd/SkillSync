package com.skillsync.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGapAnalysis {
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private Double alignmentScore;
    private Double gapPercentage;
    private Integer totalRequired;
    private Integer totalMatched;
    private Integer totalMissing;
}
