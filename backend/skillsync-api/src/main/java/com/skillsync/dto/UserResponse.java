package com.skillsync.dto;

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
    private String currentRole;
    private String targetRole;
    private List<String> skills;
    private Integer experience;
    private String goals;
    private String githubUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
