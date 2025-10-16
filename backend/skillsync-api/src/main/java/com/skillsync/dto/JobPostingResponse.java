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
public class JobPostingResponse {

    private String role;
    private List<SkillInfo> skills;
    private String source;
    private Integer totalJobsAnalyzed;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillInfo {
        private String skillName;
        private Integer demandScore;
        private String category;
        private Double frequency; // Percentage of jobs requiring this skill
    }
}
