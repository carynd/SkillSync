package com.skillsync.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerAdviceRequest {
    @JsonProperty("user_profile")
    private UserProfileRequest userProfile;

    @JsonProperty("skill_gap")
    private SkillGapRequest skillGap;

    private String question;
}
