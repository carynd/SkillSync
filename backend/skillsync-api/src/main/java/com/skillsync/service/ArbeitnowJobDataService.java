package com.skillsync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsync.dto.JobPostingResponse;
import com.skillsync.enums.JobRole;
import com.skillsync.model.SkillDemand;
import com.skillsync.repository.SkillDemandRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Real job data service using Arbeitnow API (free, no API key required)
 * API: https://www.arbeitnow.com/api/job-board-api
 */
@Service
@Primary
@Profile("dev")
@Slf4j
public class ArbeitnowJobDataService implements JobDataService {

    @Autowired
    private SkillDemandRepository skillDemandRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String ARBEITNOW_API_URL = "https://www.arbeitnow.com/api/job-board-api";

    // Common tech skills to extract from job descriptions
    private static final Set<String> KNOWN_SKILLS = new HashSet<>(Arrays.asList(
            // Programming Languages
            "Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#",
            "Ruby", "PHP", "Swift", "Kotlin", "Scala", "R", "Dart",

            // Frontend
            "React", "Angular", "Vue", "Vue.js", "Svelte", "Next.js", "Nuxt.js",
            "HTML", "CSS", "Sass", "Less", "Tailwind", "Bootstrap",

            // Backend
            "Spring Boot", "Spring", "Node.js", "Express", "Django", "Flask",
            "FastAPI", "Laravel", "Rails", ".NET", "Nest.js",

            // Databases
            "PostgreSQL", "MySQL", "MongoDB", "Redis", "Cassandra", "DynamoDB",
            "SQL", "NoSQL", "Elasticsearch", "Oracle", "MariaDB",

            // Cloud & DevOps
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "Git",
            "CI/CD", "Terraform", "Ansible", "GitHub Actions", "GitLab",

            // Mobile
            "Android", "iOS", "React Native", "Flutter", "Xamarin",

            // Data & AI
            "Machine Learning", "AI", "Deep Learning", "TensorFlow", "PyTorch",
            "Data Science", "Pandas", "NumPy", "Scikit-learn",

            // Other
            "REST API", "GraphQL", "Microservices", "Agile", "Scrum",
            "Jira", "Linux", "Unit Testing", "Jest", "JUnit"
    ));

    @Override
    public JobPostingResponse analyzeJobMarket(String role, String location, Integer maxResults) {
        log.info("🌍 ARBEITNOW MODE: Analyzing job market for role: {}", role);

        try {
            // Fetch jobs from Arbeitnow API
            String searchQuery = role.replace(" Engineer", "").toLowerCase();
            Map<String, Integer> skillFrequency = fetchAndAnalyzeJobs(searchQuery, maxResults);

            if (skillFrequency.isEmpty()) {
                log.warn("No skills extracted from Arbeitnow. Using mock data.");
                return createMockResponse(role);
            }

            // Save to database
            saveSkillsToDatabase(role, skillFrequency, "Arbeitnow");

            // Convert to response
            int totalSkills = skillFrequency.values().stream().mapToInt(Integer::intValue).sum();
            List<JobPostingResponse.SkillInfo> skillInfos = skillFrequency.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(20)
                    .map(entry -> {
                        int count = entry.getValue();
                        double frequency = totalSkills > 0 ? (count * 100.0 / totalSkills) : 0;
                        int demandScore = Math.min(95, 50 + (count * 5)); // Scale to 50-95

                        return JobPostingResponse.SkillInfo.builder()
                                .skillName(entry.getKey())
                                .demandScore(demandScore)
                                .category(categorizeSkill(entry.getKey()))
                                .frequency(frequency)
                                .build();
                    })
                    .collect(Collectors.toList());

            return JobPostingResponse.builder()
                    .role(role)
                    .skills(skillInfos)
                    .source("Arbeitnow (Real Jobs)")
                    .totalJobsAnalyzed(maxResults)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error fetching job data from Arbeitnow: {}", e.getMessage());
            return createMockResponse(role);
        }
    }

    private Map<String, Integer> fetchAndAnalyzeJobs(String searchQuery, Integer limit) {
        log.info("📡 Fetching jobs from Arbeitnow API for: {}", searchQuery);

        try {
            // Build URL with query parameters
            String url = ARBEITNOW_API_URL + "?search=" +
                    java.net.URLEncoder.encode(searchQuery, java.nio.charset.StandardCharsets.UTF_8) +
                    "&page=1";

            log.debug("Requesting URL: {}", url);
            String response = restTemplate.getForObject(url, String.class);

            if (response == null || response.isEmpty()) {
                log.warn("Empty response from Arbeitnow");
                return new HashMap<>();
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode jobs = root.get("data");

            if (jobs == null || !jobs.isArray()) {
                log.warn("No jobs data in response. Response: {}", response.substring(0, Math.min(200, response.length())));
                return new HashMap<>();
            }

            log.info("✅ Found {} jobs from Arbeitnow", jobs.size());

            Map<String, Integer> skillCount = new HashMap<>();
            int jobsAnalyzed = 0;

            for (JsonNode job : jobs) {
                if (jobsAnalyzed >= limit) break;

                String description = job.has("description") ? job.get("description").asText() : "";
                String title = job.has("title") ? job.get("title").asText() : "";
                String tags = job.has("tags") ? job.get("tags").toString() : "";

                String fullText = (title + " " + description + " " + tags).toLowerCase();

                // Extract skills from job description
                for (String skill : KNOWN_SKILLS) {
                    if (fullText.contains(skill.toLowerCase())) {
                        skillCount.put(skill, skillCount.getOrDefault(skill, 0) + 1);
                    }
                }

                jobsAnalyzed++;
            }

            log.info("📊 Extracted {} unique skills from {} jobs", skillCount.size(), jobsAnalyzed);
            return skillCount;

        } catch (Exception e) {
            log.error("Error parsing Arbeitnow response: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    private void saveSkillsToDatabase(String role, Map<String, Integer> skillFrequency, String source) {
        for (Map.Entry<String, Integer> entry : skillFrequency.entrySet()) {
            String skill = entry.getKey();
            int count = entry.getValue();
            int demandScore = Math.min(95, 50 + (count * 5));

            Optional<SkillDemand> existing = skillDemandRepository.findByRoleAndSkill(role, skill);

            if (existing.isPresent()) {
                SkillDemand demand = existing.get();
                demand.setDemandScore(demandScore);
                demand.setSource(source);
                demand.setUpdatedAt(LocalDateTime.now());
                skillDemandRepository.save(demand);
            } else {
                SkillDemand demand = SkillDemand.builder()
                        .role(role)
                        .skill(skill)
                        .demandScore(demandScore)
                        .category(categorizeSkill(skill))
                        .source(source)
                        .build();
                skillDemandRepository.save(demand);
            }
        }

        log.info("✅ Saved {} skills for role: {} from {}", skillFrequency.size(), role, source);
    }

    private String categorizeSkill(String skill) {
        if (Arrays.asList("Java", "Python", "JavaScript", "TypeScript", "Go", "Rust", "C++", "C#", "Ruby", "PHP", "Swift", "Kotlin", "Scala").contains(skill)) {
            return "Programming Language";
        } else if (Arrays.asList("React", "Angular", "Vue.js", "Spring Boot", "Django", "Flask", "Express", "Next.js").contains(skill)) {
            return "Framework";
        } else if (Arrays.asList("PostgreSQL", "MySQL", "MongoDB", "Redis", "SQL", "NoSQL").contains(skill)) {
            return "Database";
        } else if (Arrays.asList("AWS", "Azure", "GCP", "Docker", "Kubernetes").contains(skill)) {
            return "Cloud & DevOps";
        } else if (Arrays.asList("REST API", "GraphQL", "Microservices").contains(skill)) {
            return "Architecture";
        } else if (Arrays.asList("Git", "Jenkins", "Jira", "Linux").contains(skill)) {
            return "Tool";
        } else {
            return "Other";
        }
    }

    @Override
    public List<SkillDemand> getTopSkillsForRole(String role) {
        log.info("🌍 ARBEITNOW MODE: Getting top skills for role: {}", role);
        List<SkillDemand> skills = skillDemandRepository.findTopSkillsByRole(role);

        // If no skills in DB, fetch and analyze
        if (skills.isEmpty()) {
            log.info("No skills in database for {}. Fetching from Arbeitnow...", role);
            analyzeJobMarket(role, "Remote", 50);
            skills = skillDemandRepository.findTopSkillsByRole(role);
        }

        return skills;
    }

    @Override
    public List<String> getAvailableRoles() {
        // Get all roles from JobRole enum
        List<String> defaultRoles = Arrays.stream(JobRole.values())
                .map(JobRole::getDisplayName)
                .toList();

        List<String> dbRoles = skillDemandRepository.findAllDistinctRoles();

        // Filter out invalid roles (like "string") and keep only valid ones
        List<String> validRoles = dbRoles.stream()
                .filter(role -> !role.trim().isEmpty() && defaultRoles.contains(role))
                .sorted()
                .distinct()
                .collect(java.util.stream.Collectors.toList());

        // Return defaultRoles if:
        // 1. No roles in DB (validRoles is empty), OR
        // 2. Not enough roles in DB (less than 5) - means DB not fully seeded yet
        if (validRoles.isEmpty() || validRoles.size() < 5) {
            return defaultRoles;
        }

        // Otherwise return the filtered DB roles (if DB has been properly seeded)
        return validRoles;
    }

    private JobPostingResponse createMockResponse(String role) {
        log.info("Creating fallback mock response for: {}", role);

        List<JobPostingResponse.SkillInfo> mockSkills = Arrays.asList(
                JobPostingResponse.SkillInfo.builder().skillName("JavaScript").demandScore(90).category("Programming Language").frequency(85.0).build(),
                JobPostingResponse.SkillInfo.builder().skillName("React").demandScore(85).category("Framework").frequency(75.0).build(),
                JobPostingResponse.SkillInfo.builder().skillName("Node.js").demandScore(80).category("Runtime").frequency(70.0).build(),
                JobPostingResponse.SkillInfo.builder().skillName("Git").demandScore(88).category("Tool").frequency(80.0).build()
        );

        return JobPostingResponse.builder()
                .role(role)
                .skills(mockSkills)
                .source("Mock Data (Fallback)")
                .totalJobsAnalyzed(10)
                .build();
    }
}
