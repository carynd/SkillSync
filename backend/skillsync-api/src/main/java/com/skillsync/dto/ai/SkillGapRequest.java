package com.skillsync.dto.ai;

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
    private Double skillGapPercentage;
    private Double alignmentScore;
    private List<String> missingSkills;
    private Integer matchingSkillsCount;
    private Integer missingSkillsCount;
}
