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
public class UserProfileRequest {
    @JsonProperty("user_id")
    private String userId;

    private String name;

    @JsonProperty("current_role")
    private String currentRole;

    @JsonProperty("target_role")
    private String targetRole;

    @JsonProperty("current_skills")
    private List<String> currentSkills;

    @JsonProperty("experience_years")
    private Integer experienceYears;
}
