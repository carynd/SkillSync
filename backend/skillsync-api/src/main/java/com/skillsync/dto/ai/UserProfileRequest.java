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
public class UserProfileRequest {
    private String userId;
    private String name;
    private String currentRole;
    private String targetRole;
    private List<String> currentSkills;
    private Integer experienceYears;
}
