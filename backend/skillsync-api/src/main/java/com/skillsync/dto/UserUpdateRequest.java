package com.skillsync.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.skillsync.config.ExperienceLevelDeserializer;
import com.skillsync.config.JobRoleDeserializer;
import com.skillsync.enums.ExperienceLevel;
import com.skillsync.enums.JobRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @JsonDeserialize(using = JobRoleDeserializer.class)
    private JobRole currentRole;

    @JsonDeserialize(using = JobRoleDeserializer.class)
    private JobRole targetRole;

    private List<String> skills;

    @JsonDeserialize(using = ExperienceLevelDeserializer.class)
    private ExperienceLevel experienceLevel;

    private String goals;

    private String githubUsername;
}
