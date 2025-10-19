package com.skillsync.dto;

import com.skillsync.enums.ExperienceLevel;
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

    private String currentRole;

    private String targetRole;

    private List<String> skills;

    private ExperienceLevel experienceLevel;

    private String goals;

    private String githubUsername;
}
