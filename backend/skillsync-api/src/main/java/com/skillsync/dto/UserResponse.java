package com.skillsync.dto;

import com.skillsync.enums.ExperienceLevel;
import com.skillsync.enums.JobRole;
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
public class UserResponse {

    private UUID userId;
    private String name;
    private String email;
    private JobRole currentRole;
    private JobRole targetRole;
    private List<String> skills;
    private ExperienceLevel experienceLevel;
    private String goals;
    private String githubUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
