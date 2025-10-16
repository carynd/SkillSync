package com.skillsync.service;

import com.skillsync.dto.JobPostingResponse;
import com.skillsync.model.SkillDemand;
import com.skillsync.repository.SkillDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock implementation of JobDataService for development and testing.
 * Uses pre-defined skill data instead of real API calls.
 */
@Service
@Profile("dev")
@Slf4j
public class MockJobDataService implements JobDataService {

    @Autowired
    private SkillDemandRepository skillDemandRepository;

    // Mock data for different roles
    private static final Map<String, List<SkillData>> ROLE_SKILLS = new HashMap<>();

    static {
        // Backend Engineer
        ROLE_SKILLS.put("Backend Engineer", Arrays.asList(
                new SkillData("Java", 95, "Programming Language"),
                new SkillData("Spring Boot", 88, "Framework"),
                new SkillData("PostgreSQL", 82, "Database"),
                new SkillData("Redis", 75, "Database"),
                new SkillData("Docker", 78, "DevOps"),
                new SkillData("Kubernetes", 65, "DevOps"),
                new SkillData("AWS", 80, "Cloud"),
                new SkillData("Microservices", 72, "Architecture"),
                new SkillData("REST API", 90, "Architecture"),
                new SkillData("Git", 85, "Tool")
        ));

        // Frontend Engineer
        ROLE_SKILLS.put("Frontend Engineer", Arrays.asList(
                new SkillData("JavaScript", 95, "Programming Language"),
                new SkillData("React", 90, "Framework"),
                new SkillData("TypeScript", 85, "Programming Language"),
                new SkillData("HTML/CSS", 92, "Web Technology"),
                new SkillData("Redux", 70, "State Management"),
                new SkillData("Webpack", 60, "Build Tool"),
                new SkillData("Jest", 65, "Testing"),
                new SkillData("Tailwind CSS", 75, "Framework"),
                new SkillData("Next.js", 72, "Framework"),
                new SkillData("Git", 85, "Tool")
        ));

        // Full Stack Engineer
        ROLE_SKILLS.put("Full Stack Engineer", Arrays.asList(
                new SkillData("JavaScript", 92, "Programming Language"),
                new SkillData("React", 88, "Framework"),
                new SkillData("Node.js", 85, "Runtime"),
                new SkillData("Python", 75, "Programming Language"),
                new SkillData("PostgreSQL", 80, "Database"),
                new SkillData("MongoDB", 72, "Database"),
                new SkillData("Docker", 70, "DevOps"),
                new SkillData("AWS", 75, "Cloud"),
                new SkillData("REST API", 90, "Architecture"),
                new SkillData("Git", 88, "Tool")
        ));

        // Data Scientist
        ROLE_SKILLS.put("Data Scientist", Arrays.asList(
                new SkillData("Python", 98, "Programming Language"),
                new SkillData("Machine Learning", 95, "Domain"),
                new SkillData("SQL", 85, "Database"),
                new SkillData("Pandas", 90, "Library"),
                new SkillData("NumPy", 88, "Library"),
                new SkillData("TensorFlow", 80, "Framework"),
                new SkillData("Scikit-learn", 85, "Library"),
                new SkillData("Jupyter", 82, "Tool"),
                new SkillData("Statistics", 92, "Domain"),
                new SkillData("Data Visualization", 78, "Domain")
        ));

        // DevOps Engineer
        ROLE_SKILLS.put("DevOps Engineer", Arrays.asList(
                new SkillData("Docker", 95, "Container"),
                new SkillData("Kubernetes", 90, "Orchestration"),
                new SkillData("AWS", 88, "Cloud"),
                new SkillData("Jenkins", 75, "CI/CD"),
                new SkillData("Terraform", 82, "Infrastructure"),
                new SkillData("Ansible", 70, "Automation"),
                new SkillData("Linux", 92, "Operating System"),
                new SkillData("Python", 78, "Programming Language"),
                new SkillData("Git", 85, "Tool"),
                new SkillData("Monitoring", 80, "Domain")
        ));

        // Product Manager
        ROLE_SKILLS.put("Product Manager", Arrays.asList(
                new SkillData("Product Strategy", 95, "Domain"),
                new SkillData("Agile", 90, "Methodology"),
                new SkillData("User Research", 85, "Domain"),
                new SkillData("Data Analysis", 80, "Domain"),
                new SkillData("SQL", 70, "Database"),
                new SkillData("Roadmapping", 88, "Domain"),
                new SkillData("Stakeholder Management", 92, "Soft Skill"),
                new SkillData("Jira", 82, "Tool"),
                new SkillData("Figma", 75, "Tool"),
                new SkillData("Communication", 95, "Soft Skill")
        ));
    }

    @Override
    public JobPostingResponse analyzeJobMarket(String role, String location, Integer maxResults) {
        log.info("🎭 MOCK MODE: Analyzing job market for role: {}", role);

        List<SkillData> skillData = ROLE_SKILLS.getOrDefault(role, new ArrayList<>());

        if (skillData.isEmpty()) {
            log.warn("No mock data found for role: {}. Using default skills.", role);
            skillData = getDefaultSkills();
        }

        // Save to database
        saveSkillsToDatabase(role, skillData);

        // Convert to response
        List<JobPostingResponse.SkillInfo> skillInfos = skillData.stream()
                .map(sd -> JobPostingResponse.SkillInfo.builder()
                        .skillName(sd.name)
                        .demandScore(sd.score)
                        .category(sd.category)
                        .frequency((double) sd.score)
                        .build())
                .collect(Collectors.toList());

        return JobPostingResponse.builder()
                .role(role)
                .skills(skillInfos)
                .source("Mock Data (Development)")
                .totalJobsAnalyzed(100)
                .build();
    }

    @Override
    public List<SkillDemand> getTopSkillsForRole(String role) {
        log.info("🎭 MOCK MODE: Getting top skills for role: {}", role);
        return skillDemandRepository.findTopSkillsByRole(role);
    }

    @Override
    public List<String> getAvailableRoles() {
        return new ArrayList<>(ROLE_SKILLS.keySet());
    }

    private void saveSkillsToDatabase(String role, List<SkillData> skillData) {
        for (SkillData sd : skillData) {
            Optional<SkillDemand> existing = skillDemandRepository.findByRoleAndSkill(role, sd.name);

            if (existing.isPresent()) {
                SkillDemand demand = existing.get();
                demand.setDemandScore(sd.score);
                demand.setCategory(sd.category);
                skillDemandRepository.save(demand);
            } else {
                SkillDemand demand = SkillDemand.builder()
                        .role(role)
                        .skill(sd.name)
                        .demandScore(sd.score)
                        .category(sd.category)
                        .source("Mock Data")
                        .build();
                skillDemandRepository.save(demand);
            }
        }
        log.info("✅ Saved {} skills for role: {}", skillData.size(), role);
    }

    private List<SkillData> getDefaultSkills() {
        return Arrays.asList(
                new SkillData("Communication", 90, "Soft Skill"),
                new SkillData("Problem Solving", 88, "Soft Skill"),
                new SkillData("Teamwork", 85, "Soft Skill"),
                new SkillData("Time Management", 80, "Soft Skill"),
                new SkillData("Leadership", 75, "Soft Skill")
        );
    }

    // Helper class for mock data
    private static class SkillData {
        String name;
        int score;
        String category;

        SkillData(String name, int score, String category) {
            this.name = name;
            this.score = score;
            this.category = category;
        }
    }
}
